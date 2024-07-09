package vn.edu.tdc.selling_medicine_app.feature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemCustomerAdapter;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemProductAdapter;

public class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
    private ItemCustomerAdapter mItemCustomerAdapter;
    private ItemProductAdapter mItemProductAdapter;

    private Drawable icon;
    private final ColorDrawable background;
    private Context context;
    RecyclerView recyclerView;


    public SwipeToDelete(ItemCustomerAdapter itemCustomerAdapter, Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.mItemCustomerAdapter = itemCustomerAdapter;
        this.context = context;
        this.icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        this.background = new ColorDrawable(Color.WHITE);
    }
    public SwipeToDelete(ItemProductAdapter itemProductAdapter, Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.mItemProductAdapter = itemProductAdapter;
        this.context = context;
        this.icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        this.background = new ColorDrawable(Color.WHITE);
    }
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
       return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //cảm biến rung
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
        ///////////////////////////////////////////////
        int position = viewHolder.getAdapterPosition();

        if (mItemProductAdapter != null) {
            mItemProductAdapter.deleteAProduct(position);
            return;
        }
        if (mItemCustomerAdapter != null) {
            mItemCustomerAdapter.deleteACustomer(position);
            return;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}

