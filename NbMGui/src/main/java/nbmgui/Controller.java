package nbmgui;

import io.BinaryFile;
import io.BinaryFileHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import machine.NoBeardMachine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Controller {
    private NoBeardMachine machine;
    private Semaphore semaphore;
    private BinaryFile objectFile;
    private Path path;
    private String input;
    private HashMap<Integer, CheckBox> programDataMap;
    private int lastProgramLine;

    @FXML
    private TextField inputView;
    @FXML
    private TextArea outputView;
    @FXML
    private ScrollPane programDataView;
    @FXML
    private Label fileTitle;
    @FXML
    private Button startButton;
    @FXML
    private Button stepButton;
    @FXML
    private Button continueButton;
    @FXML
    private Button stopButton;
    @FXML
    private ListView<String> dataMemoryView;

    public void initialize() {
        machine = new NoBeardMachine(new FxInputDevice(this), new FxOutputDevice(this));
        semaphore = new Semaphore(0);
        programDataMap = new HashMap<>();
        setDebuggerButtonsDisable(true);
        inputView.setDisable(true);
        startButton.setDisable(true);
        inputView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && inputView != null && !inputView.getText().isEmpty())
                inputIsAvailable(inputView.getText());
        });
        dataMemoryView.setFocusTraversable(false);
    }

    Semaphore getSemaphore() {
        return semaphore;
    }

    String getInput() {
        return input;
    }

    TextArea getOutputView() {
        return outputView;
    }

    TextField getInputView() {
        return inputView;
    }

    @FXML
    void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\egonm\\Documents\\DiplomaThesis\\SamplePrograms\\NbAssemblerPrograms"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("NoBeard-object Files", "*.no"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            this.path = Paths.get(selectedFile.getAbsolutePath());
            prepareObjectFile();
            machine.loadStringConstants(objectFile.getStringStorage());
            machine.loadProgram(0, objectFile.getProgram());
            dataMemoryView.getItems().clear();
        } else
            outputView.appendText("Select a NoBeard object file\n");
    }

    @FXML
    void startProgram(ActionEvent event) {
        setDebuggerButtonsDisable(false);
        dataMemoryView.getItems().clear();
        lastProgramLine = -1;
        new Thread(() -> {
            machine.runProgram(0);
            highlightNextInstructionToBeExecuted();
            Platform.runLater(() -> updateDataMemory(getDataMemory()));
        }).start();
    }

    @FXML
    void step(ActionEvent event) {
        new Thread(() -> {
            if (machine.getBreakpoints().contains(machine.getCurrentLine()))
                machine.replaceBreakInstruction();
            machine.step();
            machine.setBreakInstructionIfNeeded();
            highlightNextInstructionToBeExecuted();
            Platform.runLater(() -> updateDataMemory(getDataMemory()));
        }).start();
    }

    @FXML
    void continueToBreakpoint(ActionEvent event) {
        new Thread(() -> {
            machine.step();
            machine.setBreakInstructionIfNeeded();
            machine.runUntilNextBreakpoint();
            highlightNextInstructionToBeExecuted();
            Platform.runLater(() -> updateDataMemory(getDataMemory()));
        }).start();
    }

    @FXML
    void stopProgram(ActionEvent event) {
        setDebuggerButtonsDisable(true);
        machine.stopProgram();
        programDataMap.get(lastProgramLine).setStyle("-fx-background-color: transparent");
    }

    private void prepareObjectFile() {
        try {
            objectFile = BinaryFileHandler.open(path.toString());
        } catch (IOException ex) {
            outputView.appendText("Unable to open " + path.getFileName().toString() + "\n");
            return;
        }
        Disassembler disassembler = new Disassembler(objectFile);
        fillProgramDataView(disassembler.getProgramData());
        fileTitle.setText(path.getFileName().toString());
        startButton.setDisable(false);
        machine.removeAllBreakpoints();
    }

    private void fillProgramDataView(List<String> programDataList) {
        VBox programData = new VBox();
        for (String lineStr : programDataList) {
            CheckBox line = new CheckBox(lineStr);
            line.setPadding(new Insets(1));
            line.setOnAction((event) -> {
                if (event.getSource() instanceof CheckBox) {
                    CheckBox breakpoint = (CheckBox) event.getSource();
                    if (breakpoint.isSelected())
                        machine.addBreakpoint(getAddressOfProgramLine(breakpoint.getText()));
                    else
                        machine.removeBreakpoint(getAddressOfProgramLine(breakpoint.getText()));
                }
            });
            programData.getChildren().add(line);
            programDataMap.put(getAddressOfProgramLine(line.getText()), line);
        }
        programDataView.setContent(programData);
    }

    private int getAddressOfProgramLine(String line) {
        return Integer.valueOf(line.substring(2, 5));
    }

    private ObservableList<String> getDataMemory() {
        ObservableList<String> result = FXCollections.observableArrayList();
        result.add("\t\tAddress\t  0\t+1\t+2\t+3");
        for (int i = 0; i <= machine.getCallStack().getStackPointer(); i += 4) {
            StringBuilder line = new StringBuilder(String.format("\t\t%0" + 4 + "d\t", i));
            for (int j = i; j < i + 4; j++) {
                line.append(String.format("\t%0" + 3 + "d", machine.getDataMemory().loadByte(j)));
            }
            result.add(line.toString());
        }
        return result;
    }

    private void updateDataMemory(ObservableList<String> content) {
        dataMemoryView.setItems(content);
        dataMemoryView.setCellFactory((list) -> new ListCell<String>() {
            int framePointer = machine.getCallStack().getFramePointer();
            int stackPointer = machine.getCallStack().getStackPointer();
            static final int INDEX_OF_ADDRESS = 2;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    createLine(item);
                }
            }

            private void createLine(String line) {
                if (getIndex() == 0 && line.contains("Address")) {
                    createHeadline(line);
                } else {
                    createDataLine(line);
                }
            }

            private void createHeadline(String item) {
                setText(item);
                setStyle("-fx-background-color: #002c73;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;");
            }

            private void createDataLine(String item) {
                HBox line = new HBox();
                int firstAddressInLine = (getIndex() - 1) * 4;
                convertStringToLabels(firstAddressInLine, item).forEach(line.getChildren()::add);
                setGraphic(line);
            }

            private List<Label> convertStringToLabels(int firstAddressInLine, String line) {
                String[] lineContent = line.split("\t");
                List<Label> result = createLabelsForAddress(lineContent[INDEX_OF_ADDRESS]);
                int currentAddress = firstAddressInLine;
                for (int i = 4; i < lineContent.length; i++) {
                    if (currentAddress == framePointer) {
                        result.add(createHighlightedLabel(lineContent[i], "#0038AC"));
                    } else if (currentAddress == stackPointer) {
                        result.add(createHighlightedLabel(lineContent[i], "#AC080E"));
                    } else {
                        result.add(createNormalLabel(lineContent[i]));
                    }
                    currentAddress++;
                }
                return result;
            }

            List<Label> createLabelsForAddress(String address) {
                return new ArrayList<>(Arrays.asList(new Label("\t"), new Label("\t"), new Label(address), new Label("\t")));
            }

            Label createHighlightedLabel(String content, String bgColor) {
                Label l = new Label(content + "\t");
                l.setStyle("-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;");
                return l;
            }

            Label createNormalLabel(String content) {
                return new Label(content + "\t");
            }
        });
        setContextMenuToListView();
    }

    private void setContextMenuToListView() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewInt = new MenuItem("View Integer");
        MenuItem viewChar = new MenuItem("View characters");
        viewChar.setOnAction((event) -> convertAsciiToChar(dataMemoryView.getSelectionModel().getSelectedIndex(), dataMemoryView.getSelectionModel().getSelectedItem()));
        viewInt.setOnAction((event) -> convertAsciiToInt(dataMemoryView.getSelectionModel().getSelectedIndex(), dataMemoryView.getSelectionModel().getSelectedItem()));
        contextMenu.getItems().addAll(viewInt, viewChar);
        dataMemoryView.setContextMenu(contextMenu);
        dataMemoryView.setOnContextMenuRequested((event) -> {
            contextMenu.show(dataMemoryView, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void convertAsciiToInt(int selectedIndex, String selectedLine) {
        ObservableList<String> dataMemoryList = dataMemoryView.getItems();
        String[] lineContent = selectedLine.split("\t");
        String line = "\t\t" + lineContent[2] + "\t\t" + machine.getDataMemory().loadWord(selectedIndex) + "\t\t\t";
        dataMemoryList.set(selectedIndex, line);
        updateDataMemory(dataMemoryList);
    }

    private void convertAsciiToChar(int selectedIndex, String selectedLine) {
        ObservableList<String> dataMemoryList = dataMemoryView.getItems();
        String[] lineContent = selectedLine.split("\t");
        StringBuilder line = new StringBuilder("\t\t" + lineContent[2] + "\t");
        for (int i = 4; i < lineContent.length; i++) {
            int ascii = Integer.parseInt(lineContent[i]);
            if ((ascii != 0)) {
                line.append("\t" + (char) ascii);
            } else {
                line.append("\t" + lineContent[i]);
            }
        }
        dataMemoryList.set(selectedIndex, line.toString());
        updateDataMemory(dataMemoryList);
    }

    private void highlightNextInstructionToBeExecuted() {
        if (programDataMap.containsKey(machine.getCurrentLine()))
            programDataMap.get(machine.getCurrentLine()).setStyle("-fx-background-color: #999999");
        else
            setDebuggerButtonsDisable(true);
        if (lastProgramLine > -1 && lastProgramLine != machine.getCurrentLine())
            programDataMap.get(lastProgramLine).setStyle("-fx-background-color: transparent");
        lastProgramLine = machine.getCurrentLine();
    }

    void setDebuggerButtonsDisable(boolean state) {
        stepButton.setDisable(state);
        continueButton.setDisable(state);
        stopButton.setDisable(state);
        startButton.setDisable(!state);
    }

    private void inputIsAvailable(String providedInput) {
        getOutputView().appendText(providedInput + "\n");
        input = providedInput;
        inputView.clear();
        inputView.setDisable(true);
        setDebuggerButtonsDisable(false);
        getSemaphore().release();
    }
}