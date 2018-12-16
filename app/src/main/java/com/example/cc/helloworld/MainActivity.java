package com.example.cc.helloworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //注册数字按钮组
    private Button[] btn_nums;

    //注册操作符按钮组
    private Button[] btn_oper;

    //其他
    private Button btn_clear;
    private Button btn_delete;
    private Button btn_equal;
    private TextView te_input;

    private StringBuilder result = new StringBuilder();
    private StringBuilder temp = new StringBuilder();
    private Stack<String> stack = new Stack<>();

    private void initView() {
        btn_nums = new Button[11];
        btn_oper = new Button[5];
        btn_nums[0] = findViewById(R.id.btn_zero);
        btn_nums[1] = findViewById(R.id.btn_one);
        btn_nums[2] = findViewById(R.id.btn_two);
        btn_nums[3] = findViewById(R.id.btn_three);
        btn_nums[4] = findViewById(R.id.btn_four);
        btn_nums[5] = findViewById(R.id.btn_five);
        btn_nums[6] = findViewById(R.id.btn_six);
        btn_nums[7] = findViewById(R.id.btn_seven);
        btn_nums[8] = findViewById(R.id.btn_eight);
        btn_nums[9] = findViewById(R.id.btn_ninee);
        btn_nums[10] = findViewById(R.id.btn_point);
        btn_oper[0] = findViewById(R.id.btn_plus);
        btn_oper[1] = findViewById(R.id.btn_reduce);
        btn_oper[2] = findViewById(R.id.btn_mul);
        btn_oper[3] = findViewById(R.id.btn_excep);
        btn_oper[4] = findViewById(R.id.btn_model);
        btn_clear = findViewById(R.id.btn_clear);
        btn_delete = findViewById(R.id.btn_delete);
        btn_equal = findViewById(R.id.btn_equal);
        te_input = findViewById(R.id.et_input);

        //注册监听器
        for (int i = 0; i < btn_nums.length; i++) {
            btn_nums[i].setOnClickListener(this);
        }
        for (int i = 0; i < btn_oper.length; i++) {
            btn_oper[i].setOnClickListener(this);
        }
        btn_clear.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_equal.setOnClickListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_zero:
                result.append("0");
                temp.append("0");
                te_input.setText(result);
                break;
            case R.id.btn_one:
                result.append("1 ");
                temp.append("1");
                te_input.setText(result);
                break;
            case R.id.btn_two:
                result.append("2 ");
                temp.append("2");
                te_input.setText(result);
                break;
            case R.id.btn_three:
                result.append("3 ");
                temp.append("3");
                te_input.setText(result);

                break;
            case R.id.btn_four:
                result.append("4 ");
                temp.append("4");
                te_input.setText(result);

                break;
            case R.id.btn_five:
                result.append("5 ");
                temp.append("5");
                te_input.setText(result);

                break;
            case R.id.btn_six:
                result.append("6 ");
                temp.append("6");
                te_input.setText(result);

                break;
            case R.id.btn_seven:
                result.append("7 ");
                temp.append("7");
                te_input.setText(result);

                break;
            case R.id.btn_eight:
                result.append("8 ");
                temp.append("8");

                te_input.setText(result);
                break;
            case R.id.btn_ninee:
                result.append("9 ");
                temp.append("9");

                te_input.setText(result);
                break;
            case R.id.btn_point:
                temp.append(".");
                result.append(". ");
                te_input.setText(result);
                break;
            case R.id.btn_plus:
                if (isCorrect()) {
                    result.append("+ ");
                    stack.push(temp.toString());
                    stack.push("+");
                    te_input.setText(result);
                    temp = new StringBuilder();
                    break;
                } else
                    break;
            case R.id.btn_reduce:
                if (isCorrect()) {
                    result.append("- ");
                    stack.push(temp.toString());
                    stack.push("-");
                    te_input.setText(result);
                    temp = new StringBuilder();
                    break;
                } else
                    break;
            case R.id.btn_mul:
                if (isCorrect()) {
                    result.append("x ");
                    stack.push(temp.toString());
                    stack.push("*");
                    te_input.setText(result);
                    temp = new StringBuilder();
                    break;
                } else
                    break;
            case R.id.btn_excep:
                if (isCorrect()) {
                    result.append("/ ");
                    stack.push(temp.toString());
                    stack.push("/");
                    te_input.setText(result);
                    temp = new StringBuilder();
                    break;
                } else
                    break;
            case R.id.btn_model:
                if (isCorrect()) {
                    result.append("% ");
                    stack.push(temp.toString());
                    stack.push("%");
                    te_input.setText(result);
                    temp = new StringBuilder();
                    break;
                } else
                    break;
            case R.id.btn_equal:
                if (isCorrect()) {
                    stack.push(temp.toString());
                    temp = new StringBuilder();
                    result.append("= " + cal());
                    te_input.setText(result);
                    break;
                } else
                    break;
            case R.id.btn_clear:
                stack = new Stack<>();
                result = new StringBuilder();
                temp = new StringBuilder();
                te_input.setText("");
                break;
            case R.id.btn_delete:
                if (temp.toString().length() == 0)
                    stack.pop();
                temp = new StringBuilder(temp.toString().substring(0, temp.toString().length() - 1));
                result = new StringBuilder(result.toString().substring(0, result.toString().length() - 2));
                te_input.setText(result.toString());
            default:
                break;
        }

    }

    public String cal() {
        //以栈为媒介, 两个栈操作
        Stack<String> oper = new Stack<>();
        while (!stack.isEmpty()) {
            String s = stack.pop();
            //如果不在高优先级
            if (!Objects.equals("*", s) && !Objects.equals("/", s) && !Objects.equals("%", s)) {
                //传入到栈oper
                oper.push(s);
            } else {
                switch (s.charAt(0)) {
                    case '*':
                        oper.push(String.valueOf(Double.parseDouble(oper.pop()) * Double.parseDouble(stack.pop())));
                        break;
                    case '/':
                        oper.push(String.valueOf(Double.parseDouble(oper.pop()) / Double.parseDouble(stack.pop())));
                        break;
                    case '%':
                        oper.push(String.valueOf(Double.parseDouble(oper.pop()) % Double.parseDouble(stack.pop())));
                        break;
                }
            }
        }

        while (!oper.isEmpty()) {
            String s = oper.pop();
            if (!Objects.equals("+", s) && !Objects.equals("-", s)) {
                stack.push(s);
            } else {
                switch (s.charAt(0)) {
                    case '+':
                        stack.push(String.valueOf(Double.parseDouble(oper.pop()) + Double.parseDouble(stack.pop())));
                        break;
                    case '-':
                        stack.push(String.valueOf(Double.parseDouble(stack.pop()) - Double.parseDouble(oper.pop())));
                        break;
                }
            }
        }
        return stack.pop();
    }

    /**
     * 判断是否符合输入标准, 什么叫输入标准,
     * 举个例子, + 后面不能再写其他操作符
     *
     * @return
     */
    public boolean isCorrect() {
        //temp的长度是0的话, 那就说明之前一个是操作符
        if (temp.toString().length() == 0) {
            return false;
        }
        return true;
    }
}
