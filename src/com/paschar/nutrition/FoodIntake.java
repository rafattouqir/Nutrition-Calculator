package com.paschar.nutrition;

import java.util.ArrayList;

import com.paschar.nutrition.R;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodIntake extends Activity {
    static final String TAG = "FoodIntake";

	private GridView gridFood;
	private GridView gridIntake;
	
	private TextView txtFoodCategory;
	
	private ImageButton btnRew;
	private ImageButton btnForward;
	private int currentCategory;
	private int mFoodPositionId;
	
	protected ArrayList<FoodObject> _arrayIntake;
	protected ArrayList<FoodObject> _arrayFood;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		gridIntake = (GridView)findViewById(R.id.gridIntake);
		gridFood = (GridView)findViewById(R.id.gridFood);
		txtFoodCategory = (TextView)findViewById(R.id.txtFoodCategory);
		
		btnRew = (ImageButton)findViewById(R.id.btnRew);
		btnForward = (ImageButton)findViewById(R.id.btnForward);
		//Setup Food
		SetFoodFilter(FoodObject.FOODTYPE_GRAINS);

		//Setup Intake
		_arrayIntake = new ArrayList<FoodObject>();
		gridIntake.setOnDragListener(new BoxDragListener());
	}
	
	public void SetFoodFilter(int foodcategory)
	{
		currentCategory = foodcategory;
		if(foodcategory == FoodObject.FOODTYPE_GRAINS)
		{
			btnRew.setEnabled(false);
		}
		else if(foodcategory == FoodObject.FOODTYPE_EXTRAS)
		{
			btnForward.setEnabled(false);
		}
		else
		{
			btnRew.setEnabled(true);
			btnForward.setEnabled(true);
		}
		switch(foodcategory)
		{
			case FoodObject.FOODTYPE_GRAINS:
				_arrayFood = FoodObject.getGrains();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_grain));
				break;
			case FoodObject.FOODTYPE_FRUITS:
				_arrayFood = FoodObject.getFruits();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_fruits));
				break;
			case FoodObject.FOODTYPE_VEGETABLES:
				_arrayFood = FoodObject.getVegetables();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_vegetables));
				break;
			case FoodObject.FOODTYPE_DIARY:
				_arrayFood = FoodObject.getDairy();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_dairy));
				break;
			case FoodObject.FOODTYPE_MEAT:
				_arrayFood = FoodObject.getMeat();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_meats));
				break;
			case FoodObject.FOODTYPE_EXTRAS:
				_arrayFood = FoodObject.getExtras();
				txtFoodCategory.setText(getResources().getString(R.string.food_filter_title) + getResources().getString(R.string.category_extras));
				break;
		}
		gridFood.setAdapter(new FoodAdapter(this));
	}
	
	public void btnForward_Clicked(View target)
	{
		SetFoodFilter(currentCategory + 1);
	}
	
	public void btnRew_Clicked(View target)
	{
		SetFoodFilter(currentCategory - 1);
	}

	/**
	 * Remove food from food intake
	 * @param position
	 */
	public void RemoveFood(int position)
	{
		_arrayIntake.remove(position);
		gridIntake.setAdapter(new IntakeAdapter(this));  
	}
	
	/**
	 * Add food to the bucket where food was taken
	 * @param position: position at _arrayFood to indicate which object it is
	 */
	public void AddFood(int position)
	{
		boolean neverTaken = true;
		FoodObject foodtaken = _arrayFood.get(position);
		for(int i = 0; i < _arrayIntake.size(); i++)
		{
			if(_arrayIntake.get(i).GetFoodId() == foodtaken.GetFoodId())
			{
				_arrayIntake.get(i).AddServing();
				neverTaken = false;
				break;
			}
		}
		
		if(neverTaken)
		{
			_arrayIntake.add(foodtaken);

		}
		
		gridIntake.setAdapter(new IntakeAdapter(FoodIntake.this));  
	}
	
	public void btnCalculate_Clicked(View target)
	{
		
		String url = "http://www.ipickupsports.com/mobile/foodsummary/Default.aspx?FoodIdArray=";
		for(int i = 0; i < _arrayIntake.size(); i++)
		{
			for(int j = 0; j < _arrayIntake.get(i).GetServings(); j++)
			{
				url += String.valueOf(_arrayIntake.get(i).GetFoodId());
				url += ",";
			}
		}

		url = url.substring(0, url.length() - 1);
		
		Intent summary = new Intent(this, CalculationResult.class);
		summary.putExtra("url", url);
		startActivity(summary);
	}
	
	/**
	 * Launch Filter Types
	 */
	public void btnFilter_Click(View target)
	{
		AlertDialog alert = new AlertDialog.Builder(FoodIntake.this)
        .setTitle(getString(R.string.food_filter_title))
        .setItems(R.array.food_category, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	FoodIntake.this.SetFoodFilter(which + 1);
            }
        })
        .create();
		
		alert.show();
	}
	
	//Adapter for the left bucket
	public class FoodAdapter extends BaseAdapter{
        public FoodAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return _arrayFood.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	ImageView iconView;
        	if (convertView == null) {
        		iconView = new FoodView(mContext);
            	iconView.setBackgroundColor(color.transparent);
            	iconView.setLayoutParams(new GridView.LayoutParams(100, 100));
               	//iconView.setAdjustViewBounds(false);
                FoodIntake.this.mFoodPositionId = position;
        	}
        	else {
        		iconView = (FoodView)convertView;
        	}
        	iconView.setImageResource(_arrayFood.get(position).GetDrawableId());

        	final int clickPosition = position;
        	iconView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    ClipData data = ClipData.newPlainText("food_id", String.valueOf(clickPosition));
                    DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    FoodIntake.this.mFoodPositionId = clickPosition;
                    return true;
                }
            });
            return iconView;
        }

        private Context mContext;
	}
	
	
	
	//Adapter for the right bucket
    public class IntakeAdapter extends BaseAdapter {
        public IntakeAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return _arrayIntake.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	FrameLayout returnView = new FrameLayout(mContext);
            if (convertView == null) {
            	TextView servingText = new TextView(mContext);
            	servingText.setText(String.valueOf(_arrayIntake.get(position).GetServings()));
            	servingText.setLayoutParams(new GridView.LayoutParams(100, 100));
            	servingText.setBackgroundResource(_arrayIntake.get(position).GetDrawableId());
            	servingText.setTag(position);
            	servingText.setTextColor(Color.BLACK);
            	servingText.setOnClickListener(new View.OnClickListener() {
                 	public void onClick(View view) {
                       	TextView textView = (TextView)view;
                      	FoodIntake.this.RemoveFood(Integer.valueOf(textView.getTag().toString()));
            		}
            	});            		
            	
            	returnView.addView(servingText);
            } else {
            	returnView = (FrameLayout) convertView;
            }
            
            return returnView;
        }

        private Context mContext;
    }

    class ANRShadowBuilder extends DragShadowBuilder {
        boolean mDoAnr;

        public ANRShadowBuilder(View view, boolean doAnr) {
            super(view);
            mDoAnr = doAnr;
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            super.onDrawShadow(canvas);
        }
    }
    
    class BoxDragListener implements OnDragListener{
        boolean insideOfMe = false;
        Drawable border = null;
        //Drawable redBorder = getResources().getDrawable(R.drawable.border3);
        @Override
        public boolean onDrag(View self, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_STARTED){
                        border = self.getBackground();
                        gridIntake.setBackgroundColor(Color.CYAN);
                        gridIntake.setBackgroundColor(Color.argb(85, 58, 80, 80));
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED){ 
                        insideOfMe = true;
                        gridIntake.setBackgroundColor(Color.argb(85, 89, 72, 52));
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED){
                        insideOfMe = false;
                        gridIntake.setBackgroundColor(Color.TRANSPARENT);
                } else if (event.getAction() == DragEvent.ACTION_DROP){
                        if (insideOfMe){
                        	if(FoodIntake.this.mFoodPositionId != 0)
                        	{
                        		FoodIntake.this.AddFood(Integer.valueOf(FoodIntake.this.mFoodPositionId));
                        	}
                        }
                    	Log.i(TAG, "Dropped the icon");
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED){
                        self.setBackgroundDrawable(border);        
                    	Log.i(TAG, "Drag Ended");
                    	FoodIntake.this.mFoodPositionId = 0;
                }
                return true;
        }
}
}