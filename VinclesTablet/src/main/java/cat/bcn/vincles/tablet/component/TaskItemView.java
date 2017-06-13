/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.component;

import android.content.Context;
import android.widget.LinearLayout;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsListActivity;

public class TaskItemView extends LinearLayout {
    int mHeight;

    public TaskItemView(Context context, int itemHeight) {

        super(context);
        this.mHeight = itemHeight;
        inflate(getContext(), R.layout.item_list_task, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    public void setHeight(int itemHeight) {
        int oldHeight = mHeight;
        mHeight = itemHeight;
        if (oldHeight != mHeight) {
            requestLayout();
            invalidate();
        }
    }
}