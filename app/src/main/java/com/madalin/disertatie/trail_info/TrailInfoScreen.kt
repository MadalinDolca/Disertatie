package com.madalin.disertatie.trail_info

import android.util.Log
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.asDateAndTime
import com.madalin.disertatie.core.domain.model.Trail
import com.madalin.disertatie.core.presentation.components.ScreenTopBar
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.core.presentation.util.UiText
import com.madalin.disertatie.trail_info.action.TrailInfoAction
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrailInfoScreen(
    trailId: String?,
    viewModel: TrailInfoViewModel = koinViewModel { parametersOf(trailId) },
    onNavigateToHomeWithTrailId: (trailId: String) -> Unit,
    onGoBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            ScreenTopBar(
                title = stringResource(R.string.trail_info),
                onGoBack = { onGoBack() },
                modifier = Modifier.statusBarsPadding()
            )

            val isOwner = uiState.currentUser.id == uiState.trail?.userId
            EditableInfo(
                trail = uiState.trail,
                isOwner = isOwner,
                isLoading = uiState.isLoadingInfo,
                error = uiState.loadingInfoError,
                onAction = viewModel::handleAction
            )
            OperationButtons(
                isOwner = isOwner,
                onUpdate = { viewModel.handleAction(TrailInfoAction.Update) },
                onDelete = {
                    viewModel.handleAction(TrailInfoAction.Delete)
                    onGoBack()
                }
            )
            HorizontalDivider(modifier = Modifier.padding(Dimens.container))

            ConstInfo(
                trail = uiState.trail,
                isLoading = uiState.isLoadingInfo,
                error = uiState.loadingInfoError
            )
            HorizontalDivider(modifier = Modifier.padding(Dimens.container))

            ImagesGrid(
                imagesList = uiState.imagesUriList,
                isLoading = uiState.isLoadingImages,
                error = uiState.loadingImagesError
            )
        }

        LaunchTrailFAB(
            trail = uiState.trail,
            onClick = { onNavigateToHomeWithTrailId(it) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimens.container, bottom = Dimens.container)
                .navigationBarsPadding()
        )
    }

    /*    Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                stickyHeader {
                    ScreenTopBar(
                        title = stringResource(R.string.trail_info),
                        onGoBack = { onGoBack() },
                        modifier = Modifier.statusBarsPadding()
                    )
                }

                item {
                    val isOwner = uiState.currentUser.id == uiState.trail?.userId
                    EditableInfo(
                        trail = uiState.trail,
                        isOwner = isOwner,
                        isLoading = uiState.isLoadingInfo,
                        error = uiState.loadingInfoError,
                        onAction = viewModel::handleAction
                    )
                    OperationButtons(
                        isOwner = isOwner,
                        onUpdate = { viewModel.handleAction(TrailInfoAction.Update) },
                        onDelete = {
                            viewModel.handleAction(TrailInfoAction.Delete)
                            onGoBack()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(Dimens.container))
                }

                item {
                    ConstInfo(
                        trail = uiState.trail,
                        isLoading = uiState.isLoadingInfo,
                        error = uiState.loadingInfoError
                    )
                    HorizontalDivider(modifier = Modifier.padding(Dimens.container))
                }

                item {
                    ImagesGrid(
                        imagesUriList = uiState.imagesUriList,
                        isLoading = uiState.isLoadingImages,
                        error = uiState.loadingImagesError
                    )
                }
            }

            LaunchTrailFAB(
                trail = uiState.trail,
                onClick = { onNavigateToMap(it) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimens.container)
                    .navigationBarsPadding()
            )
        }*/
}

@Composable
private fun EditableInfo(
    trail: Trail?,
    isOwner: Boolean,
    isLoading: Boolean,
    error: UiText,
    onAction: (TrailInfoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Dimens.container),
        verticalArrangement = Arrangement.spacedBy(Dimens.separator)
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
                text = trail.name,
                placeholder = { Text(text = stringResource(R.string.add_trail_name)) },
                isOwner = isOwner,
                onAction = { onAction(TrailInfoAction.SetName(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.separator))

            // description
            if (trail.description.isNotEmpty() || isOwner) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.titleLarge
                )
                // TODO support markdown
                InfoField(
                    text = trail.description,
                    placeholder = { Text(text = stringResource(R.string.add_trail_description)) },
                    isOwner = isOwner,
                    onAction = { onAction(TrailInfoAction.SetDescription(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // visibility
            if (isOwner) {
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
private fun ConstInfo(
    trail: Trail?,
    isLoading: Boolean,
    error: UiText,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(Dimens.container)) {
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
            // dates
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.published_at) + ": " + trail.createdAt?.time?.asDateAndTime())
                Text(text = stringResource(R.string.updated_at) + ": " + trail.updatedAt?.time?.asDateAndTime())
            }
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

            /*LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(3),
                verticalItemSpacing = 5.dp,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(imagesList.size) { index ->
                    SubcomposeAsyncImage(
                        model = imagesList[index],
                        loading = { CircularProgressIndicator() },
                        contentDescription = null,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    )
                }
            }*/
        }
    }
}

@Composable
private fun InfoField(
    text: String,
    placeholder: @Composable () -> Unit,
    isOwner: Boolean,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOwner) {
        OutlinedTextField(
            value = text,
            onValueChange = { onAction(it) },
            placeholder = { placeholder() },
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        )
    } else {
        Text(
            text = text,
            modifier = modifier
        )
    }
}

@Composable
private fun LaunchTrailFAB(
    trail: Trail?,
    onClick: (trailId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (trail != null) {
        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.launch)) },
            icon = { Icon(imageVector = Icons.Rounded.RocketLaunch, contentDescription = null) },
            onClick = { onClick(trail.id) },
            modifier = modifier
        )
    }
}

@Composable
private fun OperationButtons(
    isOwner: Boolean,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOwner) {
        Row(
            modifier = modifier
                .padding(horizontal = Dimens.container)
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

            Button(onClick = { onUpdate() }) {
                Text(text = stringResource(R.string.update))
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