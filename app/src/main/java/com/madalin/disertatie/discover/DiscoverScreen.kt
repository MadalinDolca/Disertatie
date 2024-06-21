package com.madalin.disertatie.discover

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.components.NearbyTrailCard
import com.madalin.disertatie.core.presentation.components.StatusPlaceholder
import com.madalin.disertatie.core.presentation.components.StatusPlaceholderType
import com.madalin.disertatie.core.presentation.components.TrailBannerItem
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.discover.action.DiscoverAction
import org.koin.androidx.compose.koinViewModel

private const val QUERY_MIN_LENGTH = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    paddingValues: PaddingValues,
    onNavigateToTrailInfo: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            TrailSearchBar(
                query = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (searchQuery.length >= QUERY_MIN_LENGTH) {
                        viewModel.handleAction(DiscoverAction.Search(searchQuery))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = Dimens.container, start = Dimens.container, end = Dimens.container)
            )
        }

        // show nearby trails if the search query is too short
        if (searchQuery.length < QUERY_MIN_LENGTH) {
            item {
                NearbyTrailsRoot(
                    isLoading = uiState.isLoadingNearbyTrails,
                    isError = uiState.nearbyTrailsError != UiText.Empty,
                    errorMessage = uiState.nearbyTrailsError,
                    nearbyTrails = uiState.nearbyTrails,
                    onTrailClick = { onNavigateToTrailInfo(it) },
                    onRefreshClick = { viewModel.handleAction(DiscoverAction.GetNearbyTrails) }
                )
                Spacer(modifier = Modifier.height(Dimens.separator))
            }
        }

        // discover trails list OR search results
        val trails = if (searchQuery.length >= QUERY_MIN_LENGTH) uiState.searchedTrails else uiState.discoverTrails
        val lastItemBottomPadding = paddingValues.calculateBottomPadding() + Dimens.separator
        itemsIndexed(
            items = trails,
            key = { _, item -> item.id }
        ) { index, item ->
            TrailBannerItem(
                trail = item,
                currentUserId = uiState.currentUser.id,
                onClick = { onNavigateToTrailInfo(item.id) },
                modifier = Modifier.padding(
                    start = Dimens.container,
                    end = Dimens.container,
                    bottom = if (index == trails.lastIndex) lastItemBottomPadding else Dimens.separator
                )
            )
        }

        // status placeholder
        when {
            uiState.isLoadingTrails || uiState.isSearchingTrails -> item {
                StatusPlaceholder(type = StatusPlaceholderType.SEARCHING)
            }

            uiState.searchedTrails.isEmpty() && searchQuery.length >= QUERY_MIN_LENGTH -> item {
                StatusPlaceholder(
                    type = StatusPlaceholderType.EMPTY,
                    text = stringResource(R.string.could_not_find_any_trail_that_contains_the_given_query)
                )
            }

            uiState.searchError != UiText.Empty -> item {
                StatusPlaceholder(
                    type = StatusPlaceholderType.ERROR,
                    text = uiState.searchError.asString()
                )
            }
        }
    }
}

@Composable
private fun TrailSearchBar(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        OutlinedTextField(
            value = query,
            onValueChange = { onValueChange(it) },
            modifier = modifier,
            placeholder = { Text(text = stringResource(R.string.search_trails)) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search") },
            supportingText = {
                if (query.isNotEmpty() && query.length < QUERY_MIN_LENGTH) {
                    Text(text = stringResource(R.string.enter_more_than_x_characters, QUERY_MIN_LENGTH))
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
private fun NearbyTrailsRoot(
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: UiText,
    nearbyTrails: List<Trail>,
    onTrailClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    if (isError) {
        NearbyTrailsInfoBanner(
            text = errorMessage.asString(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = { onRefreshClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.container)
        )
    } else if (nearbyTrails.isEmpty()) {
        NearbyTrailsInfoBanner(
            text = stringResource(R.string.there_are_no_trails_nearby),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
            onClick = { onRefreshClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.container)
        )
    } else {
        NearbyTrails(
            isLoading = isLoading,
            trailsList = nearbyTrails,
            onTrailClick = { onTrailClick(it) },
            onRefreshClick = { onRefreshClick() }
        )
    }
}

@Composable
private fun NearbyTrails(
    isLoading: Boolean,
    trailsList: List<Trail>,
    onTrailClick: (String) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // section title with refresh button
        Row(
            modifier = Modifier.padding(horizontal = Dimens.container),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nearby_trails),
                style = MaterialTheme.typography.titleLarge,
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(20.dp)
                )
            } else {
                IconButton(onClick = { onRefreshClick() }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.separator))

        // nearby trails grid
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = modifier.height(310.dp), // TODO remove hardcoded height
            contentPadding = PaddingValues(horizontal = Dimens.container),
            horizontalArrangement = Arrangement.spacedBy(Dimens.separator),
            verticalArrangement = Arrangement.spacedBy(Dimens.separator)
        ) {
            items(items = trailsList, key = { it.id }) { trail ->
                NearbyTrailCard(
                    trail = trail,
                    onClick = { onTrailClick(trail.id) }
                )
            }
        }
    }
}

@Composable
private fun NearbyTrailsInfoBanner(
    text: String,
    colors: CardColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick() },
        modifier = modifier,
        colors = colors
    ) {
        Text(
            text = text,
            Modifier.padding(Dimens.container)
        )
    }
}