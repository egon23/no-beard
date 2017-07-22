package nbmgui;

import machine.OutputDevice;
import nbmgui.controller.Controller;

/**
 * Created by Egon on 22.07.2017.
 */
public class OutputPrinter implements OutputDevice {
    private final Controller controller;

    public OutputPrinter(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void printInt(int value, int columnwidth) {

        controller.getOutputView().appendText(String.valueOf(value));
    }

    @Override
    public void printChar(char character, int columnwidth) {
        controller.getOutputView().appendText(String.valueOf(character));
        outputBlanks(columnwidth-1);
    }

    @Override
    public void print(String str, int columnwidth) {
        controller.getOutputView().appendText(str);
        outputBlanks(columnwidth-str.length());
    }

    @Override
    public void println() {
        controller.getOutputView().appendText("\n");
    }

    private void outputBlanks(int number) {
        for (int i = 0; i < number; i++) {
            controller.getOutputView().appendText(" ");
        }
    }
}
