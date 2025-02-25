package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import eu.kanade.domain.manga.model.Manga
import eu.kanade.presentation.browse.components.GlobalSearchCardRow
import eu.kanade.presentation.browse.components.GlobalSearchEmptyResultItem
import eu.kanade.presentation.browse.components.GlobalSearchErrorResultItem
import eu.kanade.presentation.browse.components.GlobalSearchLoadingResultItem
import eu.kanade.presentation.browse.components.GlobalSearchResultItem
import eu.kanade.presentation.browse.components.GlobalSearchToolbar
import eu.kanade.presentation.components.LazyColumn
import eu.kanade.presentation.components.Scaffold
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.ui.browse.migration.search.MigrateSearchState
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchItemResult
import eu.kanade.tachiyomi.util.system.LocaleHelper

@Composable
fun MigrateSearchScreen(
    navigateUp: () -> Unit,
    state: MigrateSearchState,
    getManga: @Composable (CatalogueSource, Manga) -> State<Manga>,
    onChangeSearchQuery: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onClickSource: (CatalogueSource) -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            GlobalSearchToolbar(
                searchQuery = state.searchQuery,
                progress = state.progress,
                total = state.total,
                navigateUp = navigateUp,
                onChangeSearchQuery = onChangeSearchQuery,
                onSearch = onSearch,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        MigrateSearchContent(
            sourceId = state.manga?.source ?: -1,
            items = state.items,
            contentPadding = paddingValues,
            getManga = getManga,
            onClickSource = onClickSource,
            onClickItem = onClickItem,
            onLongClickItem = onLongClickItem,
        )
    }
}

@Composable
fun MigrateSearchContent(
    sourceId: Long,
    items: Map<CatalogueSource, GlobalSearchItemResult>,
    contentPadding: PaddingValues,
    getManga: @Composable (CatalogueSource, Manga) -> State<Manga>,
    onClickSource: (CatalogueSource) -> Unit,
    onClickItem: (Manga) -> Unit,
    onLongClickItem: (Manga) -> Unit,
) {
    LazyColumn(
        contentPadding = contentPadding,
    ) {
        items.forEach { (source, result) ->
            item {
                GlobalSearchResultItem(
                    title = if (source.id == sourceId) "▶ ${source.name}" else source.name,
                    subtitle = LocaleHelper.getDisplayName(source.lang),
                    onClick = { onClickSource(source) },
                ) {
                    when (result) {
                        is GlobalSearchItemResult.Error -> {
                            GlobalSearchErrorResultItem(message = result.throwable.message)
                        }
                        GlobalSearchItemResult.Loading -> {
                            GlobalSearchLoadingResultItem()
                        }
                        is GlobalSearchItemResult.Success -> {
                            if (result.isEmpty) {
                                GlobalSearchEmptyResultItem()
                                return@GlobalSearchResultItem
                            }

                            GlobalSearchCardRow(
                                titles = result.result,
                                getManga = { getManga(source, it) },
                                onClick = onClickItem,
                                onLongClick = onLongClickItem,
                            )
                        }
                    }
                }
            }
        }
    }
}
