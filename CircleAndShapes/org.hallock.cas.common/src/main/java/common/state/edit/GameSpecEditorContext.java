package common.state.edit;

import common.state.spec.GameSpec;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameSpecEditorContext {
    WindowStack stack;
    Creators.GameSpecCreator spec;

    void save(Path p) throws IOException {
        if (!Files.exists(p))
            Files.createDirectory(p);
        if (!Files.isDirectory(p))
            throw new IllegalStateException("Expected directory: " + p);
        CreatorParser.save(spec, p);
    }

    void load(Path p) throws IOException {
        spec = CreatorParser.parseCreator(p);
        stack.clear();
        stack.push(Editors.createEditor(this, spec, false));
    }


    void save() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showSaveDialog(stack.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            save(Paths.get(chooser.getSelectedFile().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void load() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(Paths.get(".").toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showOpenDialog(stack.getParent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            load(Paths.get(chooser.getSelectedFile().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Interfaces.Errors checkForErrors() {
        Interfaces.Errors errors = new Interfaces.Errors();
        spec.getExportErrors(spec, errors, new Interfaces.ErrorCheckParams());
        return errors;
    }

    void export() {
        GameSpec gameSpec = spec.create();
    }











//
//    void saveDefault() throws IOException {
//        save(Paths.get("./spec3"));
//    }
//    void loadDefault() throws IOException {
//        load(Paths.get("./specification"));
//    }
}
