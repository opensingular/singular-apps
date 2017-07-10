package org.opensingular.server.commons.test;

import org.opensingular.flow.core.DefinitionInfo;
import org.opensingular.flow.core.FlowMap;
import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.ProcessDefinition;
import org.opensingular.flow.core.ProcessInstance;
import org.opensingular.flow.core.builder.FlowBuilder;
import org.opensingular.flow.core.builder.FlowBuilderImpl;
import org.opensingular.flow.core.defaults.NullTaskAccessStrategy;
import org.opensingular.server.commons.flow.SingularServerTaskPageStrategy;
import org.opensingular.server.commons.wicket.view.form.FormPage;

import javax.annotation.Nonnull;

@DefinitionInfo("fooFlowWithTransitionCommons")
public class FOOFlowWithTransition extends ProcessDefinition<ProcessInstance>{

    public FOOFlowWithTransition() {
        super(ProcessInstance.class);
    }

    @Nonnull
    @Override
    protected FlowMap createFlowMap() {
        FlowBuilder flow = new FlowBuilderImpl(this);

        ITaskDefinition startbarDef  = () -> "Start bar";
        ITaskDefinition middlebarDef = () -> "Transition bar";
        ITaskDefinition endbarDef = () -> "End bar";

        flow.addHumanTask(startbarDef)
                .withExecutionPage(SingularServerTaskPageStrategy.of(FormPage.class))
                .addAccessStrategy(new NullTaskAccessStrategy());

        flow.addHumanTask(middlebarDef)
                .withExecutionPage(SingularServerTaskPageStrategy.of(FormPage.class))
                .addAccessStrategy(new NullTaskAccessStrategy());

        flow.addEnd(endbarDef);

        flow.setStart(startbarDef);

        flow.from(startbarDef).go(middlebarDef);
        flow.from(middlebarDef).go(endbarDef);

        return flow.build();
    }
}
