package com.madalin.disertatie.profile.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madalin.disertatie.R
import com.madalin.disertatie.core.domain.extension.asDate
import com.madalin.disertatie.core.presentation.components.SectionTitle
import com.madalin.disertatie.core.presentation.components.TrailBannerItem
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.profile.presentation.action.ProfileAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    viewModel: ProfileViewModel = koinViewModel(),
    onNavigateToTrailInfo: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        item {
            UserInfoCard(
                name = uiState.currentUser.email.substringBefore("@"),
                role = uiState.currentUser.role,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = Dimens.container),
                onLogoutClick = { viewModel.handleAction(ProfileAction.DoLogout) }
            )
            Spacer(modifier = Modifier.height(Dimens.separator))

            UserStats(
                joinDate = uiState.currentUser.createdAt?.time?.asDate(),
                numberOfTrails = uiState.userTrails.size
            )
            Spacer(modifier = Modifier.height(Dimens.separator * 2))

            SectionTitle(
                title = stringResource(R.string.user_trails),
                modifier = Modifier.padding(horizontal = Dimens.container)
            )
            Spacer(modifier = Modifier.height(Dimens.separator))
        }

        val lastItemBottomPadding = paddingValues.calculateBottomPadding() + Dimens.separator
        itemsIndexed(
            items = uiState.userTrails.reversed()
        ) { index, item ->
            TrailBannerItem(
                trail = item,
                currentUserId = uiState.currentUser.id,
                onClick = { onNavigateToTrailInfo(item.id) },
                modifier = Modifier.padding(
                    start = Dimens.container,
                    end = Dimens.container,
                    bottom = if (index == uiState.userTrails.lastIndex) lastItemBottomPadding
                    else Dimens.separator
                )
            )
        }
    }
}

@Composable
private fun UserInfoCard(
    name: String,
    role: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = Dimens.container)
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.container)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.explorer),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.separator))

            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stringResource(R.string.role) + ": " + role)
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { onLogoutClick() }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout")
            }
        }
    }
}

@Composable
private fun UserStats(
    joinDate: String?,
    numberOfTrails: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.container),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        joinDate?.let {
            StatsCard(
                icon = Icons.Rounded.CalendarMonth,
                text = it,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.width(Dimens.separator))

        numberOfTrails?.let {
            StatsCard(
                icon = Icons.Rounded.Route,
                text = it.toString() + " " + stringResource(R.string.trails).lowercase(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatsCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(Dimens.container)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = text)
        }
    }
}