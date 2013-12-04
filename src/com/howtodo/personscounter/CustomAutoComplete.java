package com.howtodo.personscounter;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class CustomAutoComplete extends AutoCompleteTextView { 
	 
    public CustomAutoComplete(Context context) { 
        super(context); 
        this.setThreshold(0);
    } 
 
    public CustomAutoComplete(Context arg0, AttributeSet arg1) { 
        super(arg0, arg1); 
        this.setThreshold(0);
    } 
 
    public CustomAutoComplete(Context arg0, AttributeSet arg1, int arg2) { 
        super(arg0, arg1, arg2); 
        this.setThreshold(0);
    } 
 
    @Override 
    public boolean enoughToFilter() { 
        return true; 
    } 
 
    @Override 
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) { 
        super.onFocusChanged(focused, direction, previouslyFocusedRect); 
        if (focused) { 
            performFiltering(getText(), 0);
        } 
    }
} 	