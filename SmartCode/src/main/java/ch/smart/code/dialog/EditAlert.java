package ch.smart.code.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.Arrays;

import ch.smart.code.R;

public class EditAlert extends BaseAlert implements View.OnClickListener, TextWatcher {
    
    public interface EditAlertCancelListener {
        void onClick(@NonNull EditAlert alert);
    }
    
    public interface EditAlertSubmitListener {
        void onClick(@NonNull EditAlert alert, @NonNull String content);
    }
    
    public interface EditAlertChangeListener {
        void onChange(@NonNull EditAlert alert, @NonNull String content);
    }
    
    private View loadingV;
    private EditText editV;
    private QMUIRoundButton leftBT, rightBT;
    private TextView titleV, hintV, loadingHintV;
    private EditAlertCancelListener cancelListener;
    private EditAlertSubmitListener submitListener;
    private EditAlertChangeListener changeListener;
    
    public EditAlert(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.public_alert_edit;
    }
    
    @Override
    public void initView(View rootView) {
        editV = rootView.findViewById(R.id.alert_edit);
        titleV = rootView.findViewById(R.id.alert_title);
        hintV = rootView.findViewById(R.id.alert_hint);
        leftBT = rootView.findViewById(R.id.alert_left_button);
        rightBT = rootView.findViewById(R.id.alert_right_button);
        loadingV = rootView.findViewById(R.id.alert_loading);
        loadingHintV = rootView.findViewById(R.id.alert_loading_hint);
        leftBT.setOnClickListener(this);
        rightBT.setOnClickListener(this);
        loadingV.setOnClickListener(this);
        hideLoading();
    }
    
    @Override
    public double getWidthRatio() {
        return 0.7;
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alert_left_button) {
            if (null != cancelListener) {
                cancelListener.onClick(this);
            } else {
                this.dismiss();
            }
        } else if (v.getId() == R.id.alert_right_button) {
            if (null != submitListener) {
                submitListener.onClick(this, editV.getText().toString());
            } else {
                this.dismiss();
            }
        }
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    
    }
    
    @Override
    public void afterTextChanged(Editable s) {
        if (null != changeListener) {
            changeListener.onChange(this, editV.getText().toString());
        }
    }
    
    /**
     * 获取编辑View，可根据业务情况获取操作
     */
    public EditText getEditView() {
        return editV;
    }
    
    /**
     * 获取标题View
     */
    public TextView getTitleView() {
        return titleV;
    }
    
    /**
     * 获取编辑框下方的提示View
     */
    public TextView getHintView() {
        return hintV;
    }
    
    /**
     * 设置标题
     */
    @Override
    public void setTitle(@Nullable CharSequence title) {
        titleV.setText(title);
    }
    
    @Override
    public void setTitle(int titleId) {
        titleV.setText(titleId);
    }
    
    public EditAlert setTitle(String title) {
        titleV.setText(title);
        return this;
    }
    
    public EditAlert setTitleRes(@StringRes int titleId) {
        setTitle(titleId);
        return this;
    }
    
    /**
     * 设置编辑框内容
     */
    public EditAlert setEditContent(String content) {
        editV.setText(content);
        return this;
    }
    
    public EditAlert setEditContent(@StringRes int contentId) {
        editV.setText(contentId);
        return this;
    }
    
    /**
     * 设置编辑框提示
     */
    public EditAlert setEditHint(@StringRes int hint) {
        editV.setHint(hint);
        return this;
    }
    
    public EditAlert setEditHint(String hint) {
        editV.setHint(hint);
        return this;
    }
    
    /**
     * 设置编辑框下方提示
     */
    public EditAlert setHint(@StringRes int hintId) {
        hintV.setText(hintId);
        return this;
    }
    
    public EditAlert setHint(String hint) {
        hintV.setText(hint);
        return this;
    }
    
    /**
     * 设置取消按钮监听，不设置时点击会默认关闭弹框
     */
    public EditAlert setCancelButton(String str, EditAlertCancelListener listener) {
        leftBT.setText(str);
        cancelListener = listener;
        return this;
    }
    
    public EditAlert setCancelButton(@StringRes int strId, EditAlertCancelListener listener) {
        leftBT.setText(strId);
        cancelListener = listener;
        return this;
    }
    
    /**
     * 设置提交按钮监听，不设置时点击会默认关闭弹框
     */
    public EditAlert setSubmitButton(String str, EditAlertSubmitListener listener) {
        rightBT.setText(str);
        submitListener = listener;
        return this;
    }
    
    public EditAlert setSubmitButton(@StringRes int strId, EditAlertSubmitListener listener) {
        rightBT.setText(strId);
        submitListener = listener;
        return this;
    }
    
    /**
     * 设置编辑框最小高度
     */
    public EditAlert setEditMinHeight(int height) {
        editV.setMinHeight(height);
        return this;
    }
    
    /**
     * 设置编辑框最大高度
     */
    public EditAlert setEditMaxHeight(int height) {
        editV.setMaxHeight(height);
        return this;
    }
    
    /**
     * 设置编辑框输入内容长度
     */
    public EditAlert setEditMaxLength(int length) {
        setInputFilter(new InputFilter.LengthFilter(length));
        return this;
    }
    
    /**
     * 设置编辑框输入内容行数
     */
    public EditAlert setEditMaxLines(int lines) {
        editV.setMaxLines(lines);
        return this;
    }
    
    /**
     * 设置编辑框输入超长打点位置
     */
    public EditAlert setEditEllipsize(TextUtils.TruncateAt ellipsis) {
        editV.setEllipsize(ellipsis);
        return this;
    }
    
    /**
     * 设置输入类型
     */
    public EditAlert setEditInputType(int type) {
        editV.setInputType(type);
        return this;
    }
    
    /**
     * 设置输入过滤
     */
    public EditAlert setEditFilters(@NonNull InputFilter[] filters) {
        editV.setFilters(filters);
        return this;
    }
    
    /**
     * 设置输入监听
     */
    public EditAlert setEditChangedListener(EditAlertChangeListener listener) {
        changeListener = listener;
        if (changeListener != null) {
            editV.removeTextChangedListener(this);
            editV.addTextChangedListener(this);
        }
        return this;
    }
    
    /**
     * 设置输入光标位置
     */
    public EditAlert setSelection(int index) {
        editV.setSelection(index);
        return this;
    }
    
    @Override
    public void cancel() {
        changeListener = null;
        submitListener = null;
        cancelListener = null;
        if (null != editV) {
            editV.removeTextChangedListener(this);
            KeyboardUtils.hideSoftInput(editV);
        }
        super.cancel();
    }
    
    public EditAlert showLoading(@StringRes int hintId) {
        return showLoading(getContext().getString(hintId));
    }
    
    public EditAlert showLoading(String hint) {
        loadingHintV.setText(hint);
        loadingV.setVisibility(View.VISIBLE);
        return this;
    }
    
    public EditAlert hideLoading() {
        loadingV.setVisibility(View.GONE);
        return this;
    }
    
    private void setInputFilter(InputFilter inputFilter) {
        InputFilter[] newFilter;
        InputFilter[] inputFilters = editV.getFilters();
        if (ObjectUtils.isEmpty(inputFilters)) {
            newFilter = new InputFilter[]{inputFilter};
        } else {
            newFilter = Arrays.copyOf(inputFilters, inputFilters.length + 1);
            newFilter[newFilter.length - 1] = inputFilter;
        }
        editV.setFilters(newFilter);
    }
    
}
