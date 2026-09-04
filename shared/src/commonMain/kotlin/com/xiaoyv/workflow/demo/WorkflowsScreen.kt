package com.xiaoyv.workflow.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.xiaoyv.workflow.demo.business.WorkflowSamples
import com.xiaoyv.workflow.demo.business.component.ActionWorkflowNodeList
import com.xiaoyv.workflow.demo.support.BgmTopAppBar
import com.xiaoyv.workflow.demo.support.ContentMargin
import com.xiaoyv.workflow.demo.support.ContentMarginHalf
import com.xiaoyv.workflow.demo.support.PreviewColumn
import com.xiaoyv.workflow.demo.support.Res
import com.xiaoyv.workflow.demo.support.stringResource
import com.xiaoyv.workflow.di.WorkflowRuntimeConfig
import com.xiaoyv.workflow.di.createWorkflowRuntime
import com.xiaoyv.workflow.model.log.ActionExecutionStatus
import com.xiaoyv.workflow.node.core.ActionNodeRegistry
import com.xiaoyv.workflow.platform.room.cookie.RoomActionCookiesStorage
import com.xiaoyv.workflow.port.impl.DefaultActionHttpRequestExecutor
import com.xiaoyv.workflow.ui.all.WorkflowDefaultSideEffectHost
import com.xiaoyv.workflow.ui.core.rememberWorkflowSideEffectHostState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import kotlinx.coroutines.launch
import okio.FileSystem

@Composable
fun WorkflowsRoute() {
    val cookiesStorage = remember { RoomActionCookiesStorage() }
    val httpClient = remember {
        HttpClient {
            install(HttpCookies) {
                storage = cookiesStorage
            }
            install(Logging) {
                level = LogLevel.ALL
                format = LoggingFormat.OkHttp
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("[Network] $message")
                    }
                }
            }
        }
    }

    val runtime = remember {
        createWorkflowRuntime(
            config = WorkflowRuntimeConfig(
                httpRequestExecutor = DefaultActionHttpRequestExecutor(
                    httpClient,
                ),
            ),
        )
    }

    val viewModel = remember(runtime) {
        WorkflowsViewModel(runtime.engine)
    }

    val uiState by viewModel.uiState.collectAsState()
    val effectHostState = rememberWorkflowSideEffectHostState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(coil3.network.ktor3.KtorNetworkFetcherFactory())
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.3)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache_workflow")
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .build()
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { sideEffect ->
            when (sideEffect) {
                is WorkflowsSideEffect.Action -> {
                    scope.launch {
                        val result = effectHostState.dispatch(sideEffect.effect)
                        viewModel.onIntent(WorkflowsIntent.OnSideEffectResult(sideEffect.id, result))
                    }
                }

                is WorkflowsSideEffect.Toast -> {
                    scope.launch {
                        snackbar.showSnackbar(sideEffect.message, duration = SnackbarDuration.Short)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BgmTopAppBar(title = stringResource(Res.string.workflow_title))
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbar)
            },
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                val isHeightBounded = maxHeight != Dp.Infinity

                Column(modifier = Modifier.fillMaxSize()) {
                    // 上半屏：分类与列表
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isHeightBounded) Modifier.weight(1.2f) else Modifier.height(320.dp),
                            ),
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = uiState.selectedCategory.ordinal,
                            edgePadding = ContentMargin,
                            divider = { HorizontalDivider() },
                        ) {
                            WorkflowCategory.entries.forEach { category ->
                                Tab(
                                    selected = uiState.selectedCategory == category,
                                    onClick = {
                                        viewModel.onIntent(WorkflowsIntent.SelectCategory(category))
                                    },
                                    text = {
                                        Text(text = category.label, style = MaterialTheme.typography.titleSmall)
                                    },
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            val filteredSamples = uiState.selectedCategory.samples()

                            filteredSamples.forEach { sample ->
                                ListItem(
                                    modifier = Modifier.clickable {
                                        viewModel.onIntent(WorkflowsIntent.RunSample(sample.id))
                                    },
                                    headlineContent = {
                                        Text(sample.name, style = MaterialTheme.typography.titleSmall)
                                    },
                                    supportingContent = {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(sample.description, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                text = sample.nodes.joinToString(" → ") {
                                                    it.label.ifBlank { it.type }
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        FilledTonalButton(onClick = {
                                            viewModel.onIntent(WorkflowsIntent.RunSample(sample.id))
                                        }) {
                                            Text(stringResource(Res.string.workflow_example_run))
                                        }
                                    },
                                )
                                HorizontalDivider()
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    // 下半屏：运行状态与节点卡片/日志面板
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isHeightBounded) Modifier.weight(2.8f) else Modifier.height(480.dp),
                            ),
                    ) {
                        WorkflowExampleRunPanel(
                            run = uiState.currentRun,
                            selectedTab = uiState.selectedPanelTab,
                            onSelectTab = {
                                viewModel.onIntent(WorkflowsIntent.SelectPanelTab(it))
                            },
                            nodeRegistry = runtime.registry,
                        )
                    }
                }
            }
        }

        // 副作用弹窗宿主（二次确认、输入、选择、Web、图片/视频预览等）
        WorkflowDefaultSideEffectHost(
            hostState = effectHostState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * 示例运行状态与日志/节点卡片面板。
 */
@Composable
private fun WorkflowExampleRunPanel(
    run: WorkflowExampleRunState?,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    nodeRegistry: ActionNodeRegistry,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = ContentMarginHalf),
        verticalArrangement = Arrangement.spacedBy(ContentMarginHalf),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContentMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.workflow_run_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(ContentMarginHalf)) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    label = { Text(stringResource(Res.string.workflow_view_node_graph)) },
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    label = { Text(stringResource(Res.string.workflow_view_execution_log)) },
                )
            }
        }

        if (run == null) {
            Text(
                modifier = Modifier.padding(horizontal = ContentMargin, vertical = ContentMarginHalf),
                text = stringResource(Res.string.workflow_run_empty_log),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        when (selectedTab) {
            0 -> {
                val currentWorkflow = run.workflowId?.let { WorkflowSamples.find(it) }
                if (currentWorkflow != null) {
                    ActionWorkflowNodeList(
                        workflow = currentWorkflow,
                        nodeRegistry = nodeRegistry,
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(horizontal = ContentMargin, vertical = ContentMarginHalf),
                        text = stringResource(Res.string.workflow_run_empty_log),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ContentMargin),
                    verticalArrangement = Arrangement.spacedBy(ContentMarginHalf),
                ) {
                    val (statusText, statusContainerColor, statusContentColor) = when (run.status) {
                        WORKFLOW_RUN_STATUS_RUNNING -> Triple(
                            "⏳ " + stringResource(Res.string.workflow_run_running),
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        ActionExecutionStatus.SUCCESS -> Triple(
                            "✅ " + stringResource(Res.string.workflow_run_finished),
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.onTertiaryContainer,
                        )

                        else -> Triple(
                            "❌ " + stringResource(Res.string.workflow_run_error),
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = statusContainerColor),
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = ContentMargin, vertical = ContentMarginHalf),
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = statusContentColor,
                        )
                    }

                    if (run.executedNodeIds.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                Res.string.workflow_run_nodes,
                                run.executedNodeIds.joinToString(" → "),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    run.resolvedUrl?.let { url ->
                        Text(
                            text = stringResource(Res.string.workflow_run_opened_url, url),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    run.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    if (run.outputLines.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.workflow_run_output),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            SelectionContainer {
                                Column(
                                    modifier = Modifier.padding(ContentMargin),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    run.outputLines.forEach { line ->
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun PreviewWorkflowsScreen() {
    PreviewColumn(modifier = Modifier.fillMaxSize()) {
        WorkflowsRoute()
    }
}
