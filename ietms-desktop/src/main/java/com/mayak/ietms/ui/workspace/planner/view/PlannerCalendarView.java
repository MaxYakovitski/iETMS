package com.mayak.ietms.ui.workspace.planner.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class PlannerCalendarView extends GridPane {

    private final Locale locale = Locale.UK;
    private final WeekFields weekFields = WeekFields.of(locale);

    private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(LocalDate.now());

    private final Map<LocalDate, Button> dayButtons = new HashMap<>();

    private YearMonth currentMonth = YearMonth.now();
    private Button selectedDayButton;

    public PlannerCalendarView() {
        setHgap(5);
        setVgap(5);
        setAlignment(Pos.CENTER);

        configureGridSizing();
        renderCalendar();
    }

    public YearMonth getMonth() { return currentMonth; }

    public void setMonth(YearMonth month) {
        this.currentMonth = month;
        renderCalendar();
    }

    public ObjectProperty<LocalDate> selectedDateProperty() { return selectedDate;}

    private void renderCalendar() {
        getChildren().clear();
        dayButtons.clear();
        selectedDayButton = null;

        renderHeaderRow();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate gridStart = firstOfMonth.with(DayOfWeek.MONDAY);

        for (int row = 0; row < 6; row++) {
            LocalDate weekStart = gridStart.plusWeeks(row);

            int weekNumber = weekStart.get(weekFields.weekOfWeekBasedYear());
            add(makeWeekLabel(weekNumber), 0, row + 1);

            for (int col = 0; col < 7; col++) {
                LocalDate date = weekStart.plusDays(col);
                add(makeDayCell(date), col + 1, row + 1);
            }
        }

        restoreSelected();
    }

    private void renderHeaderRow() {
        Label wk = new Label("Week");
        wk.getStyleClass().add("calendar-header");
        GridPane.setHalignment(wk, HPos.CENTER);
        GridPane.setValignment(wk, VPos.CENTER);
        add(wk, 0, 0);

        DayOfWeek[] days = DayOfWeek.values();

        for (int i = 0; i < 7; i++) {
            DayOfWeek dow = days[i];
            Label lbl = new Label(
                    dow.getDisplayName(TextStyle.SHORT, locale)
            );
            lbl.getStyleClass().add("calendar-header");
            GridPane.setHalignment(lbl, HPos.CENTER);
            GridPane.setValignment(lbl, VPos.CENTER);
            add(lbl, i + 1, 0);
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
        getColumnConstraints().clear();

        ColumnConstraints weekCol = new ColumnConstraints();
        weekCol.setMinWidth(32);
        weekCol.setPrefWidth(32);
        weekCol.setMaxWidth(32);
        weekCol.setHgrow(Priority.NEVER);
        getColumnConstraints().add(weekCol);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setFillWidth(true);
            getColumnConstraints().add(dayCol);
        }

        // 6 радкоў
        getRowConstraints().clear();
        for (int i = 0; i < 7; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            getRowConstraints().add(rc);
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

        Button btn = dayButtons.get(date);
        if (btn == null) return;

        selectedDayButton = btn;
        btn.getStyleClass().add("calendar-day--selected");
    }
}
