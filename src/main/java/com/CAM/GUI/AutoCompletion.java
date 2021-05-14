package com.CAM.GUI;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.scene.control.TextField;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AutoCompletion {

    private final HashMap<TextField, Binding> bindings;
    private static AutoCompletion instance;

    private AutoCompletion() {
        bindings = new HashMap<>();
    }

    public static AutoCompletion getInstance() {
        if (instance == null) instance = new AutoCompletion();
        return instance;
    }

    public Binding addNewBinding(TextField textField) {
        Set<String> autoCompletions = new HashSet<>(Collections.emptyList());
        SuggestionProvider<String> provider = SuggestionProvider.create(autoCompletions);
        AutoCompletionTextFieldBinding<String> textFieldBinding = new AutoCompletionTextFieldBinding<>(textField, provider);

        Binding binding = new Binding(textField, textFieldBinding, provider);

        bindings.put(textField, binding);

        return binding;
    }

    public void removeBinding(TextField textField) {
        bindings.remove(textField);
    }

    public void updateSuggestions(TextField textField, Set<String> suggestions) {
        Binding binding = bindings.get(textField);
        binding.getProvider().clearSuggestions();
        binding.getProvider().addPossibleSuggestions(suggestions);
    }

    public Binding getBindingForTextField(TextField textField) {
        return bindings.get(textField);
    }

    public static void setDelay(long delay) {
        final AutoCompletion instance = getInstance();
        for (TextField textField : instance.bindings.keySet()) {
            Binding binding = instance.bindings.get(textField);
            binding.getTextFieldBinding().setDelay(delay);
        }
    }

}

class Binding {

    private final AutoCompletionTextFieldBinding<String> textFieldBinding;
    private final SuggestionProvider<String> provider;
    private final TextField textField;


    public Binding(TextField textField, AutoCompletionTextFieldBinding<String> textFieldBinding, SuggestionProvider<String> provider) {
        this.textField = textField;
        this.textFieldBinding = textFieldBinding;
        this.provider = provider;
    }


    public AutoCompletionTextFieldBinding<String> getTextFieldBinding() {
        return textFieldBinding;
    }

    public SuggestionProvider<String> getProvider() {
        return provider;
    }

    public TextField getTextField() {
        return textField;
    }
}
