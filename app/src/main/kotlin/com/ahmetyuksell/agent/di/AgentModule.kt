package com.ahmetyuksell.agent.di

import com.ahmetyuksell.agent.agent.AgentOrchestratorImpl
import com.ahmetyuksell.agent.agent.ToolRegistryImpl
import com.ahmetyuksell.agent.agent.tools.*
import com.ahmetyuksell.agent.domain.agent.AgentOrchestrator
import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentModule {

    @Binds @Singleton
    abstract fun bindToolRegistry(impl: ToolRegistryImpl): ToolRegistry

    @Binds @Singleton
    abstract fun bindAgentOrchestrator(impl: AgentOrchestratorImpl): AgentOrchestrator

    // Tool multibindings — each @IntoSet contributes to the Set<Tool> injected into ToolRegistryImpl
    @Binds @IntoSet abstract fun bindCalculatorTool(t: CalculatorTool): Tool
    @Binds @IntoSet abstract fun bindDateTimeTool(t: DateTimeTool): Tool
    @Binds @IntoSet abstract fun bindFileReadTool(t: FileReadTool): Tool
    @Binds @IntoSet abstract fun bindHttpRequestTool(t: HttpRequestTool): Tool
    @Binds @IntoSet abstract fun bindWebSearchTool(t: WebSearchTool): Tool
    @Binds @IntoSet abstract fun bindFinanceTool(t: FinanceTool): Tool
    @Binds @IntoSet abstract fun bindNewsTool(t: NewsTool): Tool
    @Binds @IntoSet abstract fun bindWeatherTool(t: WeatherTool): Tool
}
