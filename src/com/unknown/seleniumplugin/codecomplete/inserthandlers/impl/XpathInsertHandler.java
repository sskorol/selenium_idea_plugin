package com.unknown.seleniumplugin.codecomplete.inserthandlers.impl;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.unknown.seleniumplugin.codecomplete.inserthandlers.ISeleniumInsertHandler;
import com.unknown.seleniumplugin.domain.SeleniumCompletionVariant;

/**
 * Created by mike-sid on 22.08.14.
 */
public class XpathInsertHandler implements ISeleniumInsertHandler {

    @Override
    public void handleInsert(InsertionContext context, LookupElement lookupItem) {
        final Object object = lookupItem.getObject();
        final Editor editor = context.getEditor();
        final CaretModel caretModel = editor.getCaretModel();
        if(object instanceof SeleniumCompletionVariant) {
            SeleniumCompletionVariant completionVariant = (SeleniumCompletionVariant)object;
            caretModel.moveToOffset(caretModel.getOffset() - completionVariant.getVariantString().length() + completionVariant.getCaretOffset());
        }
    }

}
