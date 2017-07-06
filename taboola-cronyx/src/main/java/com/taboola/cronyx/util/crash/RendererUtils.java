package com.taboola.cronyx.util.crash;

import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TreeElement;

public abstract class RendererUtils {



    public static void addTreeLabel(TreeElement treeElement, String key, String value){

        treeElement.addChild(
                new RowElement().add(
                        new LabelElement(key).style(Style.Composite.style(Decoration.bold_off)),
                        new LabelElement(": "),
                        new LabelElement(value).style(Style.Composite.style(true, false, false, null, null))
                )
        );

    }

}
