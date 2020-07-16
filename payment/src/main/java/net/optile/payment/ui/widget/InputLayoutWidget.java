/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.widget;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import net.optile.payment.R;
import net.optile.payment.core.PaymentException;
import net.optile.payment.form.Operation;
import net.optile.payment.ui.widget.input.EditTextInputMode;
import net.optile.payment.validation.ValidationResult;

/**
 * Base class for widgets using the TextInputLayout and TextInputEditText
 */
public abstract class InputLayoutWidget extends FormWidget {
    final TextInputEditText textInput;
    final TextInputLayout textLayout;

    EditTextInputMode mode;

    /**
     * Construct a new TextInputWidget
     *
     * @param name name identifying this widget
     * @param rootView the root view of this input
     */
    InputLayoutWidget(String name, View rootView) {
        super(name, rootView);
        textLayout = rootView.findViewById(R.id.textinputlayout);
        textLayout.setHelperTextEnabled(true);
        textLayout.setErrorEnabled(true);

        textInput = rootView.findViewById(R.id.textinputedittext);
        textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleOnKeyboardDone();
                }
                return false;
            }
        });

        textInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                handleOnFocusChange(hasFocus);
            }
        });
    }

    public void setLabel(String label) {
        textLayout.setHintAnimationEnabled(false);
        textLayout.setHint(label);
        textLayout.setHintAnimationEnabled(true);
    }

    public void setHelperText(String helperText) {
        textLayout.setHelperText(helperText);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setLastImeOptionsWidget() {
        textInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFocus() {
        if (textInput.hasFocus()) {
            textInput.clearFocus();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValidation() {

        if (textInput.hasFocus() || TextUtils.isEmpty(getValue())) {
            setInputLayoutState(VALIDATION_UNKNOWN, false, null);
            return;
        }
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate() {
        ValidationResult result = presenter.validate(name, getValue(), null);
        return setValidationResult(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putValue(Operation operation) throws PaymentException {
        String val = getValue();
        if (!TextUtils.isEmpty(val)) {
            operation.putValue(name, val);
        }
    }

    void handleOnFocusChange(boolean hasFocus) {
        if (hasFocus) {
            setInputLayoutState(VALIDATION_UNKNOWN, false, null);
        } else if (state == VALIDATION_UNKNOWN && !TextUtils.isEmpty(getValue())) {
            validate();
        }
    }

    void handleOnKeyboardDone() {
        textInput.clearFocus();
        presenter.hideKeyboard();
    }

    void setTextInputMode(EditTextInputMode mode) {
        if (this.mode != null) {
            this.mode.reset();
        }
        this.mode = mode;
        mode.apply(textInput);
    }

    String getValue() {
        CharSequence cs = textInput.getText();
        String val = cs != null ? cs.toString().trim() : "";
        return mode != null ? mode.normalize(val) : val;
    }

    boolean setValidationResult(ValidationResult result) {
        if (result == null) {
            return false;
        }
        if (result.isError()) {
            setInputLayoutState(VALIDATION_ERROR, true, result.getMessage());
            return false;
        }
        setInputLayoutState(VALIDATION_OK, false, null);
        return true;
    }

    void setInputLayoutState(int state, boolean errorEnabled, String message) {
        setValidationState(state);
        textLayout.setError(errorEnabled ? message : null);
    }
}
