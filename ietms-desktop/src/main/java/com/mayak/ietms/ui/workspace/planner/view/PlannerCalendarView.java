package com.mayak.ietms.ui.workspace.planner.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlannerCalendarView extends VBox {

    private final Locale locale = Locale.UK;
    private final WeekFields weekFields = WeekFields.of(locale);

    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(LocalDate.now());
    private final Map<LocalDate, Button> dayButtons = new HashMap<>();

    private YearMonth currentMonth = YearMonth.now();
    private Button selectedDayButton;

    private final Label yearLabel  = new Label();
    private final Label monthLabel = new Label();

    private final GridPane grid = new GridPane();

    public PlannerCalendarView() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);
        configureGridSizing();

        Button leftButton  = createNavButton("/icons/left-chevron.png");
        Button rightButton = createNavButton("/icons/right-chevron.png");

        leftButton.setOnAction(e -> navigate(-1));
        rightButton.setOnAction(e -> navigate(1));

        yearLabel.setStyle("-fx-text-fill: #306eed; -fx-font-size: 20");

        HBox nav = new HBox(10, leftButton, monthLabel, rightButton);
        nav.setAlignment(Pos.CENTER);

        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        getChildren().addAll(yearLabel, nav, grid);

        updateHeader();
        renderCalendar();
    }

    /**
     * Observable property holding the currently selected date.
     * Fires on every date selection including programmatic ones.
     */
    public ObjectProperty<LocalDate> selectedDateProperty() {
        return selectedDate;
    }

    /**
     * Resets the calendar to today's date and current month.
     * Fires the {@code selectedDate} property change even if today was already selected.
     */
    public void resetToToday() {
        LocalDate today = LocalDate.now();
        currentMonth = YearMonth.now();
        updateHeader();
        if (today.equals(selectedDate.get())) {
            selectedDate.set(null);
        }
        selectedDate.set(today);
        renderCalendar();
    }

    private void navigate(int months) {
        currentMonth = currentMonth.plusMonths(months);
        updateHeader();
        renderCalendar();
    }

    private void updateHeader() {
        yearLabel.setText(String.valueOf(currentMonth.getYear()));
        monthLabel.setText(currentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, locale));
    }

    private Button createNavButton(String iconPath) {
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);

        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setOpacity(0.65);
        btn.setMnemonicParsing(false);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-padding: 0;"
        );
        return btn;
    }

    private void renderCalendar() {
        grid.getChildren().clear();
        dayButtons.clear();
        selectedDayButton = null;

        renderHeaderRow();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate gridStart = firstOfMonth.with(DayOfWeek.MONDAY);

        for (int row = 0; row < 6; row++) {
            LocalDate weekStart = gridStart.plusWeeks(row);

            int weekNumber = weekStart.get(weekFields.weekOfWeekBasedYear());
            grid.add(makeWeekLabel(weekNumber), 0, row + 1);

            for (int col = 0; col < 7; col++) {
                LocalDate date = weekStart.plusDays(col);
                grid.add(makeDayCell(date), col + 1, row + 1);
            }
        }

        restoreSelected();
    }

    private void renderHeaderRow() {
        Label wk = new Label("Week");
        wk.getStyleClass().add("calendar-header");
        GridPane.setHalignment(wk, HPos.CENTER);
        GridPane.setValignment(wk, VPos.CENTER);
        grid.add(wk, 0, 0);

        DayOfWeek[] days = DayOfWeek.values();

        for (int i = 0; i < 7; i++) {
            DayOfWeek dow = days[i];
            Label lbl = new Label(
                    dow.getDisplayName(TextStyle.SHORT, locale)
            );
            lbl.getStyleClass().add("calendar-header");
            GridPane.setHalignment(lbl, HPos.CENTER);
            GridPane.setValignment(lbl, VPos.CENTER);
            grid.add(lbl, i + 1, 0);
        }
    }

    private Label makeWeekLabel(int weekNumber) {
        Label lbl = new Label(String.valueOf(weekNumber));
        lbl.getStyleClass().add("calendar-week");
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setTextAlignment(TextAlignment.CENTER);
        GridPane.setHgrow(lbl, Priority.ALWAYS);
        GridPane.setVgrow(lbl, Priority.ALWAYS);
        return lbl;
    }

    private StackPane makeDayCell(LocalDate date) {
        Button btn = new Button(String.valueOf(date.getDayOfMonth()));
        btn.getStyleClass().add("calendar-day");
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        btn.setOnAction(e -> selectDate(date, btn));

        if (!YearMonth.from(date).equals(currentMonth)) {
            btn.getStyleClass().add("calendar-day--other-month");
            btn.setDisable(true);
        }

        if (date.equals(LocalDate.now())) {
            btn.getStyleClass().add("calendar-day--today");
        }

        dayButtons.put(date, btn);

        StackPane wrapper = new StackPane(btn);
        wrapper.getStyleClass().add("calendar-day-wrapper");

        wrapper.setPrefSize(48, 48);
        wrapper.setMaxSize(48, 48);
        GridPane.setHalignment(wrapper, HPos.CENTER);
        GridPane.setValignment(wrapper, VPos.CENTER);

        return wrapper;
    }

    private void configureGridSizing() {
        grid.getColumnConstraints().clear();

        ColumnConstraints weekCol = new ColumnConstraints();
        weekCol.setMinWidth(32);
        weekCol.setPrefWidth(32);
        weekCol.setMaxWidth(32);
        weekCol.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(weekCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setFillWidth(true);
            grid.getColumnConstraints().add(dayCol);
        }

        grid.getRowConstraints().clear();
        for (int i = 0; i < 7; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            grid.getRowConstraints().add(rc);
        }
    }

    private void selectDate(LocalDate date, Button btn) {
        if (selectedDayButton != null) {
            selectedDayButton.getStyleClass().remove("calendar-day--selected");
        }

        selectedDayButton = btn;
        selectedDayButton.getStyleClass().add("calendar-day--selected");

        selectedDate.set(date);
    }

    private void restoreSelected() {
        LocalDate date = selectedDate.get();
        if (date == null) return;
        if (!YearMonth.from(date).equals(currentMonth)) return;

        Button btn = dayButtons.get(date);
        if (btn == null) return;

        selectedDayButton = btn;
        btn.getStyleClass().add("calendar-day--selected");
    }
}