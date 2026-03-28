package com.example.point24;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "Point24Prefs";
    private static final String KEY_DIFFICULTY = "difficulty";

    private int difficulty = 1;
    private List<Double> currentNumbers = new ArrayList<>();
    private List<List<Double>> history = new ArrayList<>();
    private String hintSolution = null;

    private int selectedNumberIndex = -1;
    private String selectedOperator = null;

    private TextView tvDifficulty;
    private TextView tvCurrentResult;
    private TextView tvMessage;
    private Button[] numberButtons = new Button[4];
    private Button btnAdd, btnSubtract, btnMultiply, btnDivide;

    private SharedPreferences prefs;

    // 颜色常量
    private static final int COLOR_SELECTED = 0xFFF44336;  // 红色
    private static final int COLOR_DEFAULT = 0xFF9E9E9E;   // 灰色
    private static final int COLOR_OPERATOR_SELECTED = 0xFF2196F3; // 蓝色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        try {
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            difficulty = getIntent().getIntExtra(MainActivity.EXTRA_DIFFICULTY, 1);
            if (difficulty < 1 || difficulty > 3) difficulty = 1;

            initViews();
            setupListeners();
            startNewGame();
        } catch (Exception e) {
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvCurrentResult = findViewById(R.id.tvCurrentResult);
        tvMessage = findViewById(R.id.tvMessage);

        numberButtons[0] = findViewById(R.id.btnNum1);
        numberButtons[1] = findViewById(R.id.btnNum2);
        numberButtons[2] = findViewById(R.id.btnNum3);
        numberButtons[3] = findViewById(R.id.btnNum4);

        btnAdd = findViewById(R.id.btnAdd);
        btnSubtract = findViewById(R.id.btnSubtract);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnDivide = findViewById(R.id.btnDivide);

        String diffText = "难度: " + (difficulty == 1 ? "入门" : difficulty == 2 ? "容易" : difficulty == 3 ? "中等" : "困难");
        if (tvDifficulty != null) tvDifficulty.setText(diffText);
    }

    private void setupListeners() {
        for (int i = 0; i < numberButtons.length; i++) {
            if (numberButtons[i] != null) {
                final int index = i;
                numberButtons[i].setOnClickListener(v -> onNumberSelected(index));
            }
        }

        if (btnAdd != null) btnAdd.setOnClickListener(v -> onOperatorSelected("+"));
        if (btnSubtract != null) btnSubtract.setOnClickListener(v -> onOperatorSelected("-"));
        if (btnMultiply != null) btnMultiply.setOnClickListener(v -> onOperatorSelected("×"));
        if (btnDivide != null) btnDivide.setOnClickListener(v -> onOperatorSelected("÷"));

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        View btnUndo = findViewById(R.id.btnUndo);
        if (btnUndo != null) btnUndo.setOnClickListener(v -> undo());
        
        View btnRestart = findViewById(R.id.btnRestart);
        if (btnRestart != null) btnRestart.setOnClickListener(v -> startNewGame());
        
        View btnHint = findViewById(R.id.btnHint);
        if (btnHint != null) btnHint.setOnClickListener(v -> showHint());
    }

    private void startNewGame() {
        currentNumbers = generateNumbers(difficulty);
        history.clear();
        selectedNumberIndex = -1;
        selectedOperator = null;
        
        // 计算这题的提示
        hintSolution = findHint(new ArrayList<>(currentNumbers));
        
        if (tvMessage != null) tvMessage.setText("");
        
        updateDisplay();
        resetAllNumberColors();
        history.add(new ArrayList<>(currentNumbers));
    }
    
    // 判断数字是否为整数
    private boolean isInteger(double num) {
        return num == Math.floor(num) && !Double.isInfinite(num);
    }

    // 查找解题提示（传入当前难度）
    private String findHint(List<Double> numbers) {
        return findHint(numbers, difficulty);
    }
    
    private String findHint(List<Double> numbers, int diff) {
        if (numbers.size() == 1) {
            if (Math.abs(numbers.get(0) - 24) < 0.001) {
                return formatNumber(numbers.get(0));
            }
            return null;
        }

        for (int i = 0; i < numbers.size(); i++) {
            for (int j = i + 1; j < numbers.size(); j++) {
                List<Double> remaining = new ArrayList<>();
                for (int k = 0; k < numbers.size(); k++) {
                    if (k != i && k != j) {
                        remaining.add(numbers.get(k));
                    }
                }

                double a = numbers.get(i);
                double b = numbers.get(j);

                // 尝试加法（结果必须是整数）
                double addResult = a + b;
                if (isInteger(addResult) && addResult >= 0) {
                    List<Double> next = new ArrayList<>(remaining);
                    next.add(addResult);
                    String result = findHint(next, diff);
                    if (result != null) {
                        return formatNumber(a) + " + " + formatNumber(b) + " = " + formatNumber(addResult) + ", " + result;
                    }
                }

                // 尝试减法 a-b（结果必须是整数，不能是负数）
                double sub1Result = a - b;
                if (isInteger(sub1Result) && sub1Result >= 0) {
                    List<Double> next = new ArrayList<>(remaining);
                    next.add(sub1Result);
                    String result = findHint(next, diff);
                    if (result != null) {
                        return formatNumber(a) + " - " + formatNumber(b) + " = " + formatNumber(sub1Result) + ", " + result;
                    }
                }

                // 尝试减法 b-a（结果必须是整数，不能是负数）
                double sub2Result = b - a;
                if (isInteger(sub2Result) && sub2Result >= 0) {
                    List<Double> next = new ArrayList<>(remaining);
                    next.add(sub2Result);
                    String result = findHint(next, diff);
                    if (result != null) {
                        return formatNumber(b) + " - " + formatNumber(a) + " = " + formatNumber(sub2Result) + ", " + result;
                    }
                }

                // 尝试乘法（结果必须是整数）
                double mulResult = a * b;
                if (isInteger(mulResult) && mulResult >= 0) {
                    List<Double> next = new ArrayList<>(remaining);
                    next.add(mulResult);
                    String result = findHint(next, diff);
                    if (result != null) {
                        return formatNumber(a) + " × " + formatNumber(b) + " = " + formatNumber(mulResult) + ", " + result;
                    }
                }

                // 入门难度不使用除法
                if (diff != 1) {
                    // 尝试除法 a/b（结果必须是整数）
                    if (b != 0 && a % b == 0) {
                        double div1Result = a / b;
                        List<Double> next = new ArrayList<>(remaining);
                        next.add(div1Result);
                        String result = findHint(next, diff);
                        if (result != null) {
                            return formatNumber(a) + " ÷ " + formatNumber(b) + " = " + formatNumber(div1Result) + ", " + result;
                        }
                    }

                    // 尝试除法 b/a（结果必须是整数）
                    if (a != 0 && b % a == 0) {
                        double div2Result = b / a;
                        List<Double> next = new ArrayList<>(remaining);
                        next.add(div2Result);
                        String result = findHint(next, diff);
                        if (result != null) {
                            return formatNumber(b) + " ÷ " + formatNumber(a) + " = " + formatNumber(div2Result) + ", " + result;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void showHint() {
        if (hintSolution != null) {
            // 去掉末尾的 ", 24" 或 ",24"
            String hint = hintSolution;
            if (hint.endsWith(", 24")) {
                hint = hint.substring(0, hint.length() - 4);
            } else if (hint.endsWith(",24")) {
                hint = hint.substring(0, hint.length() - 3);
            }
            if (tvMessage != null) tvMessage.setText("提示: " + hint);
        } else {
            if (tvMessage != null) tvMessage.setText("此题无解");
        }
    }
    
    // 重置所有数字颜色为白色
    private void resetAllNumberColors() {
        for (int i = 0; i < numberButtons.length; i++) {
            if (numberButtons[i] != null && i < currentNumbers.size()) {
                numberButtons[i].setBackgroundColor(COLOR_DEFAULT);  // 灰色背景
                numberButtons[i].setTextColor(0xFFFFFFFF);  // 白色字体
            }
        }
        // 同时重置运算符颜色
        clearOperatorHighlights();
    }

    private List<Double> generateNumbers(int difficulty) {
        int maxValue;
        if (difficulty == 1) {
            maxValue = 9;  // 入门: 1-9
        } else if (difficulty == 2) {
            maxValue = 9;  // 容易: 1-9
        } else if (difficulty == 3) {
            maxValue = 10;  // 中等: 1-10
        } else {
            maxValue = 13;  // 困难: 1-13
        }
        Random random = new Random();
        
        for (int attempt = 0; attempt < 100; attempt++) {
            List<Double> nums = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                nums.add((double) (random.nextInt(maxValue) + 1));
            }
            if (canMake24(nums, difficulty)) {
                return nums;
            }
        }
        
        List<Double> nums = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            nums.add((double) (random.nextInt(10) + 1));
        }
        return nums;
    }

    private boolean canMake24(List<Double> numbers, int difficulty) {
        if (numbers.size() == 1) {
            return Math.abs(numbers.get(0) - 24) < 0.001;
        }

        for (int i = 0; i < numbers.size(); i++) {
            for (int j = i + 1; j < numbers.size(); j++) {
                List<Double> remaining = new ArrayList<>();
                for (int k = 0; k < numbers.size(); k++) {
                    if (k != i && k != j) {
                        remaining.add(numbers.get(k));
                    }
                }

                double a = numbers.get(i);
                double b = numbers.get(j);

                // 加法
                List<Double> next = new ArrayList<>(remaining);
                next.add(a + b);
                if (canMake24(next, difficulty)) return true;

                // 减法（结果不能是负数）
                if (a - b >= 0) {
                    next = new ArrayList<>(remaining);
                    next.add(a - b);
                    if (canMake24(next, difficulty)) return true;
                }
                if (b - a >= 0) {
                    next = new ArrayList<>(remaining);
                    next.add(b - a);
                    if (canMake24(next, difficulty)) return true;
                }

                // 乘法
                next = new ArrayList<>(remaining);
                next.add(a * b);
                if (canMake24(next, difficulty)) return true;

                // 除法：入门难度不使用除法
                if (difficulty != 1) {
                    if (b != 0 && a % b == 0) {
                        next = new ArrayList<>(remaining);
                        next.add(a / b);
                        if (canMake24(next, difficulty)) return true;
                    }
                    if (a != 0 && b % a == 0) {
                        next = new ArrayList<>(remaining);
                        next.add(b / a);
                        if (canMake24(next, difficulty)) return true;
                    }
                }
            }
        }
        return false;
    }

    private void onNumberSelected(int index) {
        if (index >= currentNumbers.size()) return;

        if (selectedOperator == null) {
            selectedNumberIndex = index;
            highlightSelectedNumber(index);
            if (tvMessage != null) tvMessage.setText("请选择运算符");
        } else {
            if (selectedNumberIndex == index) {
                if (tvMessage != null) tvMessage.setText("请选择不同的数字");
                return;
            }
            performCalculation(selectedNumberIndex, index, selectedOperator);
        }
    }

    private void onOperatorSelected(String operator) {
        if (selectedNumberIndex == -1) {
            if (tvMessage != null) tvMessage.setText("请先选择一个数字");
            return;
        }
        selectedOperator = operator;
        highlightSelectedOperator(operator);
        if (tvMessage != null) tvMessage.setText("请选择第二个数字");
    }

    private void performCalculation(int index1, int index2, String operator) {
        double num1 = currentNumbers.get(index1);
        double num2 = currentNumbers.get(index2);
        double result = 0;
        boolean valid = true;

        switch (operator) {
            case "+": result = num1 + num2; break;
            case "-": result = num1 - num2; break;
            case "×": result = num1 * num2; break;
            case "÷":
                if (num2 == 0) {
                    if (tvMessage != null) tvMessage.setText("除数不能为0！");
                    valid = false;
                } else {
                    result = num1 / num2;
                }
                break;
        }

        if (!valid) return;

        history.add(new ArrayList<>(currentNumbers));

        List<Double> newNumbers = new ArrayList<>();
        for (int i = 0; i < currentNumbers.size(); i++) {
            if (i == index1) {
                newNumbers.add(result);
            } else if (i != index2) {
                newNumbers.add(currentNumbers.get(i));
            }
        }

        currentNumbers = newNumbers;
        selectedNumberIndex = 0;
        selectedOperator = null;
        
        clearOperatorHighlights();
        
        updateDisplay();
        
        highlightSelectedNumber(0);
        
        // 计算完成后，需要先选择运算符再选择下一个数字
        selectedNumberIndex = 0;  // 结果被选中
        selectedOperator = null;  // 但没有选运算符
        
        // 提示用户选择运算符
        if (tvMessage != null) tvMessage.setText("请选择运算符");
        
        checkResult();
    }

    private void undo() {
        if (history.isEmpty()) {
            if (tvMessage != null) tvMessage.setText("没有可撤销的操作");
            return;
        }

        currentNumbers = history.remove(history.size() - 1);
        selectedNumberIndex = -1;
        selectedOperator = null;
        clearAllHighlights();

        updateDisplay();
        if (tvMessage != null) tvMessage.setText("已撤销");
    }

    private void checkResult() {
        if (currentNumbers.size() == 1) {
            double result = currentNumbers.get(0);
            if (Math.abs(result - 24) < 0.001) {
                if (tvMessage != null) tvMessage.setText("恭喜！你成功计算出24点！");
                Toast.makeText(this, "正确！🎉", Toast.LENGTH_LONG).show();

                if (prefs != null) {
                    prefs.edit().putInt(KEY_DIFFICULTY, difficulty).apply();
                }

                findViewById(R.id.btnRestart).postDelayed(this::startNewGame, 2000);
            } else {
                if (tvMessage != null) {
                    tvMessage.setText("计算错误，结果是 " + formatNumber(result) + "，不是24");
                }
            }
        }
    }

    private String formatNumber(double num) {
        if (num == (int) num) {
            return String.valueOf((int) num);
        }
        return String.format("%.2f", num);
    }

    private void updateDisplay() {
        for (int i = 0; i < numberButtons.length; i++) {
            if (numberButtons[i] != null) {
                if (i < currentNumbers.size()) {
                    numberButtons[i].setText(formatNumber(currentNumbers.get(i)));
                    numberButtons[i].setVisibility(View.VISIBLE);
                } else {
                    numberButtons[i].setVisibility(View.INVISIBLE);
                }
            }
        }

        if (currentNumbers.size() > 0 && tvCurrentResult != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentNumbers.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatNumber(currentNumbers.get(i)));
            }
            tvCurrentResult.setText("当前: " + sb.toString());
        }
    }

    // 高亮选中的数字（红色背景+红色字体）
    private void highlightSelectedNumber(int index) {
        for (int i = 0; i < numberButtons.length; i++) {
            if (numberButtons[i] != null && i < currentNumbers.size()) {
                if (i == index) {
                    numberButtons[i].setBackgroundColor(COLOR_SELECTED);  // 红色背景
                    numberButtons[i].setTextColor(COLOR_SELECTED);  // 红色字体
                } else {
                    numberButtons[i].setBackgroundColor(COLOR_DEFAULT);   // 灰色背景
                    numberButtons[i].setTextColor(0xFFFFFFFF);  // 白色字体
                }
            }
        }
    }

    // 高亮选中的运算符（蓝色）
    private void highlightSelectedOperator(String operator) {
        clearOperatorHighlights();
        switch (operator) {
            case "+": if (btnAdd != null) btnAdd.setBackgroundColor(COLOR_OPERATOR_SELECTED); break;
            case "-": if (btnSubtract != null) btnSubtract.setBackgroundColor(COLOR_OPERATOR_SELECTED); break;
            case "×": if (btnMultiply != null) btnMultiply.setBackgroundColor(COLOR_OPERATOR_SELECTED); break;
            case "÷": if (btnDivide != null) btnDivide.setBackgroundColor(COLOR_OPERATOR_SELECTED); break;
        }
    }

    private void clearOperatorHighlights() {
        if (btnAdd != null) btnAdd.setBackgroundColor(COLOR_DEFAULT);
        if (btnSubtract != null) btnSubtract.setBackgroundColor(COLOR_DEFAULT);
        if (btnMultiply != null) btnMultiply.setBackgroundColor(COLOR_DEFAULT);
        if (btnDivide != null) btnDivide.setBackgroundColor(COLOR_DEFAULT);
    }

    private void clearAllHighlights() {
        for (int i = 0; i < numberButtons.length; i++) {
            if (numberButtons[i] != null) {
                numberButtons[i].setBackgroundColor(COLOR_DEFAULT);
            }
        }
        clearOperatorHighlights();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}