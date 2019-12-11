package common.state.edit;

import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        GameSpecEditorContext context = new GameSpecEditorContext();

//        context.spec = CreatorParser.parseCreator(Paths.get("./spec2"));
        context.stack = new WindowStack(context);

        if (false) {
            context.load(Paths.get("./spec2"));
            context.save(Paths.get("./specification"));
        }

        context.load(Paths.get("./specification"));

        JFrame  frame = new JFrame();
        frame.setBounds(50, 50, 500, 500);
        frame.setTitle("Spec editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(context.stack.getParent());
        frame.setVisible(true);





        //        GameSpecParser.parseGameSpec(Paths.get("./spec"));
    }
}
