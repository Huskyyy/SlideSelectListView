package com.mwang.slideselectlistview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Wang on 2016/1/4.
 */
public class SlideSelectListAdapter extends RecyclerView.Adapter<SlideSelectListAdapter.ViewHolder>{

    // The scroll status for continuing to scroll down
    private static final int CONTINUE_SCROLL_DOWN=1;
    // The scroll status for continuing to scroll up
    private static final int CONTINUE_SCROLL_UP=2;
    // The scroll status for stop
    private static final int SCROLL_STOP=3;
    // Record the status for the scroll event
    private int scrollStatus;

    // The scroll step size
    private static final int SCROLL_STEP=16;

    // The position of the first item user touched with ACTION_DOWN
    private int downPosition;
    // The last position user touched
    private int lastPosition;
    // The current postion user touched
    private int currentPosition;

    // The status of the slide gesture
    private int moveStatus;
    // User touches down
    private static final int NO_MOVED=1;
    // User touches town and has not moved out the downPosition
    private static final int HAS_MOVED=2;
    // User touches down and moves, currentPosition!=downPosition
    private static final int HAS_MOVED_OUT=3;

    // The current context of the recyclerview
    private Context mContext;
    // The current recyclerview
    private RecyclerView mRecyclerView;
    // The data to be bound to the recyclerview
    private ArrayList<String> mDataset;
    // The multiselector for the adapter
    private MultiSelector mSelector = new MultiSelector();
    // The handler to handler the continue scroll event
    private ContinueScrollHandler continueScrollHandler = new ContinueScrollHandler();
    // The bound which triggers the continuing scrolling event on the top and the bottom of the recyclerview
    private int bound;
    // The leftborder and the rightborder of the scalable checkbox
    private float leftBorder,rightBorder;






    /**
     * Create the adapter for the recyclerview
     * @param context Current context
     * @param myDataset Data to be bound to the recyclerview
     * @param myRecyclerView The current recyclerview
     */
    public SlideSelectListAdapter(final Context context, final ArrayList myDataset, final RecyclerView myRecyclerView){
        mContext=context;
        mRecyclerView=myRecyclerView;
        mDataset=myDataset;

        for(int i=0;i<mDataset.size();i++){
            mSelector.setItemChecked(i, false);
        }

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            // Record the down point, current point and last point
            private float xDown,yDown,xMove,yMove,xLastMove,yLastMove;
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {

                // Initilize the leftborder, rightborder and the bound
                if(leftBorder==0 && rightBorder==0)
                    for(int i=0;i<myDataset.size();i++){
                        ViewHolder vh=(ViewHolder)rv.findViewHolderForAdapterPosition(i);
                        if(vh!=null){
                            leftBorder=vh.mView.getWidth()-vh.mScalableCheckBox.getWidth();
                            rightBorder=vh.mView.getWidth();
                            bound=vh.mView.getHeight()*3/4;
                            break;
                        }
                    }
                /**
                 * If the user touches the scalable checkbox and move,
                 * we return true to catch the motionevent in the onTouchEvent method.
                 * Otherwise, return false to make the child view react to the motionevent
                 */
                if(rv.getLayoutManager().getChildCount()>0){
                    int action = event.getAction();
                    switch(action){
                        case MotionEvent.ACTION_DOWN:
                            moveStatus=NO_MOVED;
                            xDown=event.getX();
                            yDown=event.getY();
                            downPosition = rv.getChildAdapterPosition(rv.findChildViewUnder(xDown, yDown));
                            xMove=xDown;
                            yMove=yDown;
                            currentPosition=downPosition;
                            xLastMove=xDown;
                            yLastMove=yDown;
                            lastPosition=downPosition;
                            return false;
                        case MotionEvent.ACTION_MOVE:
                            xMove=event.getX();
                            yMove=event.getY();
                            if(xMove>=leftBorder && xMove<=rightBorder) {
                                if (Math.abs(xMove - xDown) > 0 ) {
                                    moveStatus=HAS_MOVED;
                                }
                            }
                            if(moveStatus!=NO_MOVED){
                                return true;
                            }
                            return false;

                    }
                }
                return false;
            }

            /**
             * We handle the move event here
             */
            @Override
            public void onTouchEvent(final RecyclerView rv, final MotionEvent event) {
                xMove=event.getX();
                yMove=event.getY();

                currentPosition = rv.getChildAdapterPosition(rv.findChildViewUnder(xMove, yMove));
                if(yMove>=rv.getHeight()-bound && yMove<rv.getHeight()){
                    if(currentPosition!=getItemCount()-1 || ((ViewHolder)rv.findViewHolderForAdapterPosition(getItemCount()-1)).mView.getBottom()>rv.getHeight()){
                        if(scrollStatus!=CONTINUE_SCROLL_DOWN){
                            scrollStatus=CONTINUE_SCROLL_DOWN;
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(scrollStatus==CONTINUE_SCROLL_DOWN){
                                        Message msg = new Message();
                                        msg.what=CONTINUE_SCROLL_DOWN;
                                        continueScrollHandler.sendMessage(msg);
                                        try{
                                            Thread.sleep(10);
                                        }catch(InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            t.start();
                        }
                    }
                    if(event.getAction()==MotionEvent.ACTION_UP){
                        if(moveStatus==HAS_MOVED){
                            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(downPosition), !mSelector.isItemChecked(downPosition));
                        }
                        scrollStatus=SCROLL_STOP;
                    }
                    return;
                }else if(yMove<=bound && yMove>=0){
                    if(currentPosition!=0 || ((ViewHolder)rv.findViewHolderForAdapterPosition(0)).mView.getBottom()>rv.getHeight()){
                        if(scrollStatus!=CONTINUE_SCROLL_UP){
                            scrollStatus=CONTINUE_SCROLL_UP;
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(scrollStatus==CONTINUE_SCROLL_UP){
                                        Message msg = new Message();
                                        msg.what=CONTINUE_SCROLL_UP;
                                        continueScrollHandler.sendMessage(msg);
                                        try{
                                            Thread.sleep(10);
                                        }catch(InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            t.start();
                        }
                    }
                    if(event.getAction()==MotionEvent.ACTION_UP){
                        scrollStatus=SCROLL_STOP;
                        if(moveStatus==HAS_MOVED){
                            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(downPosition), !mSelector.isItemChecked(downPosition));
                        }
                    }
                    return;
                }

                scrollStatus=SCROLL_STOP;

                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        setMovingItem();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(moveStatus==HAS_MOVED){
                            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(downPosition), !mSelector.isItemChecked(downPosition));
                        }
                        break;
                }

                //record
                xLastMove=xMove;
                yLastMove=yMove;
                lastPosition=currentPosition;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    /**
     * The viewholder for each item view
     */
    public class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener{
        public View mView;
        public TextView mTextView;
        public ScalableCheckBox mScalableCheckBox;

        /**
         * Create a viewholder
         * @param v The item view
         */
        public ViewHolder(View v){
            super(v);
            mView=v;
            mTextView=(TextView)v.findViewById(R.id.my_textview);
            mScalableCheckBox=(ScalableCheckBox)v.findViewById(R.id.my_scalable_checkbox);

            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
            mScalableCheckBox.setOnClickListener(this);
            mScalableCheckBox.setOnLongClickListener(this);
        }

        /**
         * When the recyclerview is multi-selectable or the user touches the scalable checkbox,
         * set each item view with SetItem method.
         * Otherwise, it is a regular click.
         */
        @Override
        public void onClick(View v) {
            if (mSelector.isMultiSelectable() || v.getId() == R.id.my_scalable_checkbox) {
                setItem(this,!mSelector.isItemChecked(getAdapterPosition()));
            } else {
                Toast.makeText(mRecyclerView.getContext(), "REGULAR ONCLICK: item " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * When it triggers onLongClick, just check the item view.
         */
        @Override
        public boolean onLongClick(View v){
            setItem(this,true);
            return true;
        }

    }


    /**
     * Create the viewholder for each item view
     */
    @Override
    public SlideSelectListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    /**
     * Bind data to the viewholder according to the current position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset.get(position));
        holder.mScalableCheckBox.setChecked(mSelector.isItemChecked(position));
        if(mSelector.isMultiSelectable()){
            holder.mScalableCheckBox.expandCheckBox(false);
        }else{
            holder.mScalableCheckBox.shrinkCheckBox(false);
        }
        holder.itemView.setActivated(mSelector.isItemChecked(position));
    }

    /**
     *
     * @return the number of the item
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * @param viewHolder The viewholder to be Set
     * @param check the state for the viewholder
     */
    public void setItem(ViewHolder viewHolder, boolean check){


        switch(mSelector.getSelectedSum()){
            case 0:
                // Enter into multi-selectable mode, expand the checkbox
                if(check){
                    for(int i=0;i<mDataset.size();i++){
                        ViewHolder holder = (ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(i);
                        if(holder!=null){
                            holder.mScalableCheckBox.expandCheckBox(true);
                        }else{
                            mRecyclerView.getAdapter().notifyItemChanged(i);
                        }
                    }
                    viewHolder.mScalableCheckBox.setChecked(check);
                }
                break;
            case 1:
                // Leave the multi-selectable mode, shrink the checkbox
                if (mSelector.isItemChecked(viewHolder.getAdapterPosition()) && !check){
                    viewHolder.mScalableCheckBox.setChecked(check);
                    for(int i=0;i<mDataset.size();i++){
                        ViewHolder holder = (ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(i);
                        if(holder!=null){
                            holder.mScalableCheckBox.shrinkCheckBox(true);
                        }else{
                            mRecyclerView.getAdapter().notifyItemChanged(i);
                        }
                    }
                // Otherwise, still in the multi-selectable mode
                }else{
                    viewHolder.mScalableCheckBox.setChecked(check);
                }
                break;
            default:
                viewHolder.mScalableCheckBox.setChecked(check);
                break;
        }

        mSelector.setItemChecked(viewHolder.getAdapterPosition(), check);
        viewHolder.mView.setActivated(check);

    }

    /**
     * When the recyclerview is scrolling, we set the item view with SetMovingItem
     */
    public void setMovingItem(){

        if(currentPosition!=downPosition){
            moveStatus=HAS_MOVED_OUT;
        }
        if(lastPosition==downPosition && currentPosition-lastPosition==1){// Down 2 items
            for(int i=downPosition;i<=currentPosition;i++){
                setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i), !mSelector.isItemChecked(i));
            }
        }else if(lastPosition>downPosition && currentPosition-lastPosition==1){// Down 3 or more items
            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(currentPosition), !mSelector.isItemChecked(currentPosition));
        }else if(currentPosition>downPosition && currentPosition-lastPosition==-1){// First down then up (not at the downPosition)
            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(lastPosition), !mSelector.isItemChecked(lastPosition));
        }else if(currentPosition==downPosition && currentPosition-lastPosition==-1){// First down then up to the downPosition
            for(int i=lastPosition;i>=currentPosition;i--){
                setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i), !mSelector.isItemChecked(i));
            }
        }else if(lastPosition==downPosition && currentPosition-lastPosition==-1){// Up 2 items
            for(int i=downPosition;i>=currentPosition;i--){
                setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i), !mSelector.isItemChecked(i));
            }
        }else if(lastPosition<downPosition && currentPosition-lastPosition==-1){// Up 3 or more items
            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(currentPosition), !mSelector.isItemChecked(currentPosition));
        }else if(currentPosition<downPosition && currentPosition-lastPosition==1){// First up then down (not at the downPosition)
            setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(lastPosition), !mSelector.isItemChecked(lastPosition));
        }else if(currentPosition==downPosition && currentPosition-lastPosition==1){// First up then down to the downPosition
            for(int i=lastPosition;i<=currentPosition;i++){
                setItem((ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i), !mSelector.isItemChecked(i));
            }
        }
    }

    private class ContinueScrollHandler extends Handler{


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CONTINUE_SCROLL_DOWN:
                    currentPosition=mRecyclerView.getChildAdapterPosition(mRecyclerView.findChildViewUnder(mRecyclerView.getWidth()-(rightBorder-leftBorder)/2, mRecyclerView.getHeight()-bound/2));
                    if(currentPosition!=getItemCount()-1 || ((ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(getItemCount()-1)).mView.getBottom()>mRecyclerView.getHeight()+SCROLL_STEP){
                        setMovingItem();
                        lastPosition=currentPosition;
                        mRecyclerView.scrollBy(0, SCROLL_STEP);
                    }else{
                        mRecyclerView.scrollBy(0,((ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(getItemCount()-1)).mView.getBottom()-mRecyclerView.getHeight());
                        scrollStatus=SCROLL_STOP;
                    }
                    break;
                case CONTINUE_SCROLL_UP:
                    currentPosition=mRecyclerView.getChildAdapterPosition(mRecyclerView.findChildViewUnder(mRecyclerView.getWidth()-(rightBorder-leftBorder)/2, bound/2));
                    if(currentPosition!=0 || ((ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(0)).mView.getTop()<-SCROLL_STEP){
                        setMovingItem();
                        lastPosition=currentPosition;
                        mRecyclerView.scrollBy(0, -SCROLL_STEP);
                    }else{
                        mRecyclerView.scrollBy(0,((ViewHolder)mRecyclerView.findViewHolderForAdapterPosition(0)).mView.getTop());
                        scrollStatus=SCROLL_STOP;
                    }
                    break;
                case SCROLL_STOP:
                    break;
                default:
                    break;

            }
            super.handleMessage(msg);
        }
    };


    /**
     * A helper class which records the state for each item view
     */
    private static class MultiSelector{

        private SparseBooleanArray selectedPositions=new SparseBooleanArray();
        private int selectedSum;

        private void setItemChecked(int position, boolean isChecked){
            if(isItemChecked(position)==false && isChecked==true)
                selectedSum++;
            else if(isItemChecked(position)==true && isChecked==false)
                    selectedSum--;

            selectedPositions.put(position,isChecked);
        }

        private boolean isItemChecked(int position){
            return selectedPositions.get(position,false);
        }

        private boolean isMultiSelectable(){
            return selectedSum!=0;
        }

        private int getSelectedSum(){
            return selectedSum;
        }
    }
}
