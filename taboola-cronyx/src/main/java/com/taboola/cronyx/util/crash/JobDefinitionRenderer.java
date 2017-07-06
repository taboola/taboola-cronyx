package com.taboola.cronyx.util.crash;

import com.taboola.cronyx.JobDefinition;
import com.taboola.cronyx.annotations.SpringQualifier;
import com.taboola.cronyx.annotations.UserInput;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.*;

import java.util.Arrays;
import java.util.Iterator;

import static com.taboola.cronyx.util.crash.RendererUtils.addTreeLabel;

public class JobDefinitionRenderer extends Renderer<JobDefinition> {

    @Override
    public Class<JobDefinition> getType() {
        return JobDefinition.class;
    }

    @Override
    public LineRenderer renderer(Iterator<JobDefinition> stream) {

        //
        TableElement table = new TableElement(4,1,1,4,1)
                .overflow(Overflow.WRAP)
                .rightCellPadding(1);

        // Header
        table.add(
                new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
                        "JobKey",
                        "Recoverable",
                        "Concurrent",
                        "Class",
                        "Args"
                )
        );

        boolean firstHandled = false;

        while (stream.hasNext()){
            JobDefinition jd = stream.next();
            if (stream.hasNext() || firstHandled) {  // more than one item

                table.row(
                        new LabelElement(jd.getKey().toString()),
                        new LabelElement(jd.isRecoverable()),
                        new LabelElement(jd.isConcurrentExecutionAllowed()),
                        new LabelElement(jd.getImplementingClass().getCanonicalName()),
                        new LabelElement(
                                Arrays.asList(jd.getArgs())
                                        .stream()
                                        .count()
                        )
                );

                firstHandled = true;

            } else { //only one item - return it as a tree
                return buildAsTree(jd).renderer();
            }

        }

        return table.renderer();
    }

    public static TreeElement buildAsTree(JobDefinition jd) {
        TreeElement thisJobTree = new TreeElement(jd.getKey().toString());

        addTreeLabel(thisJobTree, "Description", jd.getDescription() );
        addTreeLabel(thisJobTree, "Class", jd.getImplementingClass().getCanonicalName());
        addTreeLabel(thisJobTree, "Recoverable", String.valueOf(jd.isRecoverable()) );
        addTreeLabel(thisJobTree, "Retryable", String.valueOf(jd.isRetryable()) );
        addTreeLabel(thisJobTree, "Allows Concurrent Excecution", String.valueOf(jd.isConcurrentExecutionAllowed()) );

        TreeElement argsTree = new TreeElement("Args ");
        Arrays.asList(jd.getArgs())
                .stream()
                .filter(ad -> ad.getDescriptor() instanceof UserInput)
                .forEach(ad -> {
                    UserInput input = (UserInput) ad.getDescriptor();
                    TreeElement argTree = new TreeElement(input.name());
                    argsTree.addChild(argTree);
                    addTreeLabel(argTree, "Type", ad.getType().getSimpleName());
                    addTreeLabel(argTree, "Default", input.defaultValue());
                    addTreeLabel(argTree, "Description", input.description());
                });
        Arrays.asList(jd.getArgs())
                .stream()
                .filter(ad -> ad.getDescriptor() instanceof SpringQualifier)
                .forEach(ad -> {
                    SpringQualifier qualifier = (SpringQualifier) ad.getDescriptor();
                    TreeElement argTree = new TreeElement(qualifier.beanSource());
                    argsTree.addChild(argTree);
                    addTreeLabel(argTree, "Default Bean Name", qualifier.defaultBeanName());
                });

        thisJobTree.addChild(argsTree);
        return thisJobTree;
    }


}
