package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.DietIngredientsProductFragment;

public class DietIngredientsProductAdapter extends RecyclerView.Adapter<DietIngredientsProductAdapter.DietIngredientsProductHolder> {

    private final DietIngredientsProductFragment.ClickListener listener;
    List<SpannableStringBuilder> list;
    @BindView( R.id.toolbar )
    Toolbar toolbar;

    public DietIngredientsProductAdapter(List<SpannableStringBuilder> list, DietIngredientsProductFragment.ClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class DietIngredientsProductHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView ingredientTextView;
        private ImageButton stateGreenImageButton;
        private ImageButton stateGreyImageButton;
        private ImageButton stateOrangeImageButton;
        private ImageButton stateRedImageButton;
        private WeakReference<DietIngredientsProductFragment.ClickListener> listenerRef;

        public DietIngredientsProductHolder(View itemView, DietIngredientsProductFragment.ClickListener listener) {
            super(itemView);
            ingredientTextView = (TextView) itemView.findViewById(R.id.ingredientTextView);
            stateGreenImageButton = (ImageButton) itemView.findViewById(R.id.stateGreenImageButton);
            stateGreyImageButton = (ImageButton) itemView.findViewById(R.id.stateGreyImageButton);
            stateOrangeImageButton = (ImageButton) itemView.findViewById(R.id.stateOrangeImageButton);
            stateRedImageButton = (ImageButton) itemView.findViewById(R.id.stateRedImageButton);
            listenerRef = new WeakReference<>(listener);
            itemView.setOnClickListener(this);
            stateGreenImageButton.setOnClickListener(this);
            stateGreenImageButton.setOnLongClickListener(this);
            stateGreyImageButton.setOnClickListener(this);
            stateGreyImageButton.setOnLongClickListener(this);
            stateOrangeImageButton.setOnClickListener(this);
            stateOrangeImageButton.setOnLongClickListener(this);
            stateRedImageButton.setOnClickListener(this);
            stateRedImageButton.setOnLongClickListener(this);
        }

        public void bind(SpannableStringBuilder ingredient){
            ingredientTextView.setText(ingredient);
        }

        // onClick Listener for view
        @Override
        public void onClick(View v) {
            listenerRef.get().onPositionClicked(getAdapterPosition(), v);
        }

        //onLongClickListener for view
        @Override
        public boolean onLongClick(View v) {
            listenerRef.get().onLongClicked(getAdapterPosition(), v);
            return true;
        }

        public int stateFromView(View v) {
            int state = 2;
            if (v.getId() == stateGreenImageButton.getId()) {
                state = 1;
            } else if (v.getId() == stateOrangeImageButton.getId()) {
                state=0;
            } else if (v.getId() == stateRedImageButton.getId()) {
                state=-1;
            }
            return state;
        }

    }

    @Override
    public DietIngredientsProductHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diet_ingredients_product,viewGroup,false);
        return new DietIngredientsProductHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(DietIngredientsProductHolder dietIngredientHolder, int position) {
        SpannableStringBuilder ingredient = list.get(position);
        dietIngredientHolder.stateRedImageButton.setScaleX((float) 0.7);
        dietIngredientHolder.stateRedImageButton.setScaleY((float) 0.7);
        dietIngredientHolder.stateOrangeImageButton.setScaleX((float) 0.7);
        dietIngredientHolder.stateOrangeImageButton.setScaleY((float) 0.7);
        dietIngredientHolder.stateGreenImageButton.setScaleX((float) 0.7);
        dietIngredientHolder.stateGreenImageButton.setScaleY((float) 0.7);
        dietIngredientHolder.stateGreyImageButton.setScaleX((float) 0.7);
        dietIngredientHolder.stateGreyImageButton.setScaleY((float) 0.7);
        ForegroundColorSpan[] FGCSpans= ingredient.getSpans(0,ingredient.length(), ForegroundColorSpan.class);
        if (FGCSpans.length == 1) {
            //Only one color, activate the corresponding button.
            if (ingredient.getSpanStart(FGCSpans[0]) == 0 && ingredient.getSpanEnd(FGCSpans[0]) == ingredient.length()) {
                //And it covered the all text. activate the corresponding colored button.
                //FGCSpans[0].getForegroundColor();
                //dietIngredientHolder.stateGreenImageButton.getForeground()
                Log.i("BUTTON", "Position : " + position + " ForegroundColor : " + FGCSpans[0].getForegroundColor());
                switch (FGCSpans[0].getForegroundColor()) {
                    case -16731136 :
                        //Green, that mean State 1
                        dietIngredientHolder.stateGreenImageButton.setScaleX((float) 1.1);
                        dietIngredientHolder.stateGreenImageButton.setScaleY((float) 1.1);
                        break;
                    case -26368 :
                        //Orange, that mean State 0
                        dietIngredientHolder.stateOrangeImageButton.setScaleX((float) 1.1);
                        dietIngredientHolder.stateOrangeImageButton.setScaleY((float) 1.1);
                        break;
                    case -65536 :
                        //Red, that mean State -1
                        dietIngredientHolder.stateRedImageButton.setScaleX((float) 1.1);
                        dietIngredientHolder.stateRedImageButton.setScaleY((float) 1.1);
                        break;
                    default:
                        //Well, that mean State 2
                        dietIngredientHolder.stateGreyImageButton.setScaleX((float) 1.1);
                        dietIngredientHolder.stateGreyImageButton.setScaleY((float) 1.1);
                        break;
                }
            } else {
                //The color doesn't cover the all text. activate the grey button.
            };
        } else {
            //No colors or more than one, activate the grey button.
            dietIngredientHolder.stateGreyImageButton.setScaleX((float) 1.1);
            dietIngredientHolder.stateGreyImageButton.setScaleY((float) 1.1);
        };
        dietIngredientHolder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}