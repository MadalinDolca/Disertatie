package com.madalin.disertatie.trail_info.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.madalin.disertatie.core.domain.model.Weather
import com.madalin.disertatie.core.presentation.components.WeatherInfo
import com.madalin.disertatie.core.presentation.components.WeatherInfoMode
import com.madalin.disertatie.core.presentation.util.Dimens
import com.madalin.disertatie.trail_info.domain.model.WeatherTab
import kotlinx.coroutines.launch

@Composable
fun WeatherForecastPager(
    weatherForecast: List<Weather>,
    weatherForecastTabs: List<WeatherTab>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { weatherForecast.size })
    val tabListState = rememberLazyListState()
    val indicatorListState = rememberLazyListState()

    Column(modifier = modifier) {
        // unique dates
        DateTabs(
            tabState = tabListState,
            pagerState = pagerState,
            tabs = weatherForecastTabs,
            modifier = Modifier.fillMaxWidth()
        )

        // weather info pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Dimens.container),
            pageSpacing = Dimens.separator
        ) { page ->
            Card {
                WeatherInfo(
                    weather = weatherForecast[page],
                    mode = WeatherInfoMode.FORECAST,
                    modifier = modifier.padding(Dimens.container)
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.separator))

        // page indicator
        PageIndicator(
            indicatorState = indicatorListState,
            pagerState = pagerState,
            modifier = Modifier
                .width(105.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Dimens.container)
        )

        // scrolls the indicator to the current dot when the current page changes and keeps it at the middle
        LaunchedEffect(pagerState.currentPage) {
            coroutineScope.launch {
                val halfScreenDots = (indicatorListState.layoutInfo.visibleItemsInfo.size / 2)
                val targetIndex = if (pagerState.currentPage < halfScreenDots) {
                    0
                } else {
                    pagerState.currentPage - halfScreenDots
                }

                indicatorListState.animateScrollToItem(targetIndex)
            }
        }
    }
}

@Composable
private fun DateTabs(
    tabState: LazyListState,
    pagerState: PagerState,
    tabs: List<WeatherTab>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        state = tabState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Dimens.container),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(tabs.size) { index ->
            val tab = tabs[index]
            FilterChip(
                selected = true,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(tab.weatherIndex) } },
                label = { Text(text = tab.date) }
            )
        }
    }
}

@Composable
private fun PageIndicator(
    indicatorState: LazyListState,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = indicatorState,
        modifier = modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.Center
    ) {
        items(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(7.dp)
            )
        }
    }
}