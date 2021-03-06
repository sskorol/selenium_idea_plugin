package com.unknown.seleniumplugin.elementscheckers.existancechecker.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.unknown.seleniumplugin.elementscheckers.existancechecker.backend.CheckElementExistenceResult;
import com.unknown.seleniumplugin.elementscheckers.existancechecker.backend.WebDriverChecker;
import com.unknown.seleniumplugin.settings.SeleniumSettingsParams;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CheckElementExistenceDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField urlTextField;
    private JButton checkButton;
    private JLabel statusTextField;
    private JTextField locatorValueTextField;
    private JLabel locatorMethodTextField;
    private JButton saveNewValueButton;
    private JTextField phantomJsPathTextField;
    private String locator;
    private String findMethod;
    private Project project;
    private String newSelectorVersion;
    private PropertiesComponent properties;

    public CheckElementExistenceDialog(Project project, final String locator, final String findMethod) {
        super(project);
        this.project = project;
        this.locator = locator;
        this.findMethod = findMethod;
        this.properties = PropertiesComponent.getInstance(project);
        setStates();
        init();
        setTitle("Check Element Existence on page");

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideStatus();
                checkElement();
            }
        });

        saveNewValueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newSelectorVersion = locatorValueTextField.getText();
                disableSaveNewLocatorButton();
            }
        });

        urlTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setCheckButtonState();
            }
        });
        locatorValueTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setCheckButtonState();
                setSaveNewLocatorButtonState();
            }
        });
        locatorMethodTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setCheckButtonState();
            }
        });

    }

    private void setSaveNewLocatorButtonState() {
        String locatorTextFieldValue = locatorValueTextField.getText();
        if (!locatorTextFieldValue.isEmpty() && !locatorTextFieldValue.equals(locator)) {
            enableSaveNewLocatorButton();
        } else {
            disableSaveNewLocatorButton();
        }
    }

    private void enableSaveNewLocatorButton() {
        saveNewValueButton.setEnabled(true);
    }

    private void disableSaveNewLocatorButton() {
        saveNewValueButton.setEnabled(false);
    }


    private void checkElement() {
        final StringBuilder error = new StringBuilder();
        final StringBuilder successMessage = new StringBuilder();
        final String phantomJsFieldValue = getPhantomJsPath();
        if (phantomJsFieldValue.isEmpty()) {
            error.append("PhantomJS not configured. Please insert path to phantomJs to the specified field");
        } else {
            properties.setValue(SeleniumSettingsParams.PHANTOM_JS_PATH, phantomJsFieldValue);
            ProgressManager.getInstance().run(new Task.Modal(project, "Check status", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    indicator.pushState();
                    try {
                        indicator.setText("Waiting for element check");
                        ApplicationManager.getApplication().runReadAction(new Runnable() {
                            @Override
                            public void run() {
                                CheckElementExistenceResult result = WebDriverChecker.checkElementExist(urlTextField.getText(),
                                        locatorValueTextField.getText(), locatorMethodTextField.getText(), phantomJsFieldValue);
                                if (!result.isFound()) {
                                    String errorMessage = result.getError();
                                    if (errorMessage != null) {
                                        error.append(errorMessage);
                                    } else {
                                        error.append("Not Found");
                                    }
                                } else {
                                    successMessage.append("Found '").append(result.getElementsCount()).append("' element(s)");
                                }
                            }
                        });
                    } finally {
                        indicator.popState();
                    }
                }
            });

        }

        if (error.length() == 0) {
            showPassedStatus(successMessage.toString());
        } else {
            showErrorStatus(error.toString());
        }
    }

    private String getPhantomJsPath() {
        return phantomJsPathTextField.getText();
    }

    private void showErrorStatus(String error) {
        statusTextField.setVisible(true);
        statusTextField.setText(error);
        statusTextField.setForeground(JBColor.RED);
    }

    private void showPassedStatus(String message) {
        statusTextField.setVisible(true);
        statusTextField.setText(message);
        statusTextField.setForeground(JBColor.GREEN);
    }

    private void hideStatus() {
        statusTextField.setVisible(false);
        statusTextField.setText("");
    }

    private void setStates() {
        setLocatorElementsState();
        setCheckButtonState();
        disableSaveNewLocatorButton();
        setPhantomJsFieldValue();
    }

    private void setPhantomJsFieldValue() {
        if(properties.isValueSet(SeleniumSettingsParams.PHANTOM_JS_PATH)){
            phantomJsPathTextField.setText(properties.getValue(SeleniumSettingsParams.PHANTOM_JS_PATH));
        }
    }

    private boolean isLocatorValueFieldValid() {
        return locatorValueTextField.getText() == null || locatorValueTextField.getText().isEmpty();
    }

    private boolean isLocatorMethodFieldValud() {
        return locatorMethodTextField.getText() == null || locatorMethodTextField.getText().isEmpty();
    }

    private boolean isUrlFieldValid() {
        return urlTextField.getText() == null || urlTextField.getText().isEmpty() || isDefault(urlTextField.getText());
    }


    private void setCheckButtonState() {
        if (isLocatorValueFieldValid() || isLocatorMethodFieldValud() || isUrlFieldValid()) {
            enableCheckButton(false);
        } else {
            enableCheckButton(true);
        }
    }

    private boolean isDefault(String text) {
        return text.equals("http://");
    }

    private void setLocatorElementsState() {
        if (locator != null) {
            locatorValueTextField.setText(locator);
        }
        if (findMethod != null) {
            locatorMethodTextField.setText(findMethod);
        }
    }

    private void enableCheckButton(boolean state) {
        checkButton.setEnabled(state);
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected void doOKAction() {
        super.doOKAction();
    }

    public String getNewSelectorVersion() {
        return newSelectorVersion;
    }
}
