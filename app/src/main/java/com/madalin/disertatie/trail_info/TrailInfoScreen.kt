package com.madalin.disertatie.trail_info

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.asDateAndTime
import com.madalin.disertatie.core.domain.extension.asDuration
import com.madalin.disertatie.core.domain.extension.prettyLength
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.domain.model.TrailPoint
import com.madalin.disertatie.core.presentation.components.ScreenTopBar
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.trail_info.action.TrailInfoAction
import com.madalin.disertatie.trail_info.components.TrailPointBannerItem
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrailInfoScreen(
    trailId: String?,
    viewModel: TrailInfoViewModel = koinViewModel { parametersOf(trailId) },
    onGoBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    var isVisibleOnScroll by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    // makes isVisibleOnScroll false when the user scrolls down, and true when it scrolls up
    LaunchedEffect(key1 = lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .collect { scrollOffset ->
                isVisibleOnScroll = scrollOffset <= previousScrollOffset
                previousScrollOffset = scrollOffset
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = lazyListState) {
            stickyHeader {
                ScreenTopBar(
                    title = stringResource(R.string.trail_info),
                    onGoBack = { onGoBack() },
                    modifier = Modifier.statusBarsPadding()
                )
            }
            item {
                TrailInfo(
                    trail = uiState.trail,
                    isEditing = uiState.isEditing,
                    isLoading = uiState.isLoadingInfo,
                    error = uiState.loadingInfoError,
                    onAction = viewModel::handleAction
                )
                HorizontalDivider(modifier = Modifier.padding(Dimens.container))
            }
            pointsTimeline(
                pointsList = uiState.trailPointsList,
                distancesList = uiState.trailPointsDistances,
                isLoading = uiState.isLoadingPoints,
                error = uiState.loadingPointsError
            )
        }
        RightSideButtons(
            isOwner = uiState.currentUser.id == uiState.trail?.userId,
            isVisible = !uiState.isEditing && isVisibleOnScroll,
            onEnableEditingClick = { viewModel.handleAction(TrailInfoAction.EnableEditing) },
            onLaunchTrailClick = { viewModel.handleAction(TrailInfoAction.SetLaunchedTrailId) },
            onGoBack = { onGoBack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = Dimens.container)
                .navigationBarsPadding()
        )
        OperationButtons(
            isVisible = uiState.isEditing,
            onDelete = {
                viewModel.handleAction(TrailInfoAction.Delete)
                onGoBack()
            },
            onCancel = { viewModel.handleAction(TrailInfoAction.DisableEditing) },
            onUpdate = {
                viewModel.handleAction(TrailInfoAction.Update)
                viewModel.handleAction(TrailInfoAction.DisableEditing)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun TrailInfo(
    trail: Trail?,
    isEditing: Boolean,
    isLoading: Boolean,
    error: UiText,
    onAction: (TrailInfoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .animateContentSize()
            .padding(Dimens.container)
    ) {
        if (isLoading) {
            CircularLoader(
                text = stringResource(R.string.loading_info),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        } else if (error != UiText.Empty || trail == null) {
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // name
            InfoField(
                type = InfoFieldType.NAME,
                text = trail.name,
                placeholder = { Text(text = stringResource(R.string.add_trail_name)) },
                isEditing = isEditing,
                onAction = { onAction(TrailInfoAction.SetName(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.separator * 2))

            // metrics
            if (!isEditing) {
                TrailMetrics(trail = trail)
                Spacer(modifier = Modifier.height(Dimens.separator * 2))
            }

            // description
            if (trail.description.isNotEmpty() || isEditing) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.titleLarge
                )
                InfoField(
                    type = InfoFieldType.DESCRIPTION,
                    text = trail.description,
                    placeholder = { Text(text = stringResource(R.string.add_trail_description)) },
                    isEditing = isEditing,
                    onAction = { onAction(TrailInfoAction.SetDescription(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // visibility toggle
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.make_trail_public))
                    Switch(
                        checked = trail.public,
                        onCheckedChange = { onAction(TrailInfoAction.SetVisibility(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailMetrics(
    trail: Trail?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                icon = Icons.Rounded.Straighten,
                text = trail?.length?.prettyLength().orEmpty(),
                subText = stringResource(R.string.trail_length),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(Dimens.separator))
            InfoCard(
                icon = Icons.Rounded.Schedule,
                text = trail?.duration?.asDuration().orEmpty(),
                subText = stringResource(R.string.travel_time),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.separator * 2))
        InfoRow(
            icon = Icons.Rounded.CalendarToday,
            text = stringResource(R.string.published_at) + " " + trail?.createdAt?.time?.asDateAndTime()
        )
        InfoRow(
            icon = Icons.Rounded.CalendarMonth,
            text = stringResource(R.string.updated_at) + " " + trail?.updatedAt?.time?.asDateAndTime()
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    text: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(Dimens.container)) {
            Row {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(Dimens.separator))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = subText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(Dimens.separator))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

private fun LazyListScope.pointsTimeline(
    pointsList: List<TrailPoint>,
    distancesList: List<Float>,
    isLoading: Boolean,
    error: UiText,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        item {
            Column(modifier = modifier.padding(Dimens.container)) {
                CircularLoader(
                    text = stringResource(R.string.loading_trail_points),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    } else if (error != UiText.Empty) {
        item {
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = Dimens.container)
            )
        }
    } else {
        item {
            Text(
                text = stringResource(R.string.documented_trail_points),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Dimens.container)
            )
            Spacer(modifier = Modifier.height(Dimens.separator))
        }
        itemsIndexed(
            items = pointsList,
            key = { _, item -> item.id }
        ) { index, item ->
            if (shouldShowTrailPoint(item)) {
                TrailPointBannerItem(
                    trailPoint = item,
                    distance = distancesList[index],
                    bottomSpacing = Dimens.separator * 2
                )
            }
        }
    }
}

@Composable
private fun RightSideButtons(
    isOwner: Boolean,
    isVisible: Boolean,
    onEnableEditingClick: () -> Unit,
    onLaunchTrailClick: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = expandHorizontally(expandFrom = Alignment.Start),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.separator),
            horizontalAlignment = Alignment.End
        ) {
            if (isOwner) {
                IconButton(
                    onClick = { onEnableEditingClick() },
                    modifier = Modifier
                        .padding(end = Dimens.container)
                        .size(Dimens.iconButtonContainerSize),
                    colors = IconButtonDefaults.filledIconButtonColors()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(Dimens.iconButtonContentSize)
                    )
                }
            }
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.launch)) },
                icon = { Icon(imageVector = Icons.Rounded.RocketLaunch, contentDescription = null) },
                onClick = {
                    onLaunchTrailClick()
                    onGoBack()
                },
                modifier = Modifier.padding(end = Dimens.container)
            )
        }
    }
}

private enum class InfoFieldType {
    NAME, DESCRIPTION
}

@Composable
private fun InfoField(
    type: InfoFieldType,
    text: String,
    placeholder: @Composable () -> Unit,
    isEditing: Boolean,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditing) {
        OutlinedTextField(
            value = text,
            onValueChange = { onAction(it) },
            placeholder = { placeholder() },
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        )
    } else {
        if (type == InfoFieldType.NAME) {
            Text(
                text = text,
                modifier = modifier,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        } else {
            MarkdownText(
                markdown = text,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ImagesGrid(
    imagesList: List<String>,
    isLoading: Boolean,
    error: UiText,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = Dimens.container)) {
        if (isLoading) {
            CircularLoader(
                text = stringResource(R.string.loading_images),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        } else if (error != UiText.Empty) {
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Log.d("ImagesGrid", "imagesList: ${imagesList.size}")
            val columns = 3
            var rows = (imagesList.size / columns)
            if (imagesList.size.mod(columns) > 0) {
                rows += 1
            }

            for (rowId in 0 until rows) {
                val firstIndex = rowId * columns
                Row {
                    for (columnId in 0 until columns) {
                        val index = firstIndex + columnId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (index < imagesList.size) {
                                SubcomposeAsyncImage(
                                    model = imagesList[index],
                                    loading = { CircularProgressIndicator() },
                                    contentDescription = null,
                                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationButtons(
    isVisible: Boolean,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }),
        exit = slideOutVertically(targetOffsetY = { it / 2 }),
    ) {
        Card(modifier = Modifier.padding(horizontal = Dimens.container)) {
            Row(
                modifier = Modifier
                    .padding(Dimens.container)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FilledTonalButton(
                    onClick = { onDelete() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = stringResource(R.string.delete))
                }
                FilledTonalButton(onClick = { onCancel() }) {
                    Text(text = stringResource(R.string.cancel))
                }
                Button(onClick = { onUpdate() }) {
                    Text(text = stringResource(R.string.update))
                }
            }
        }
    }
}

@Composable
private fun CircularLoader(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Dimens.separator))
        Text(text = text)
    }
}

/**
 * Returns `true` if this [trailPoint] has sufficient data to be shown.
 */
private fun shouldShowTrailPoint(trailPoint: TrailPoint) =
    trailPoint.note.isNotEmpty()
            || trailPoint.imagesList.isNotEmpty()
            || trailPoint.hasWarning