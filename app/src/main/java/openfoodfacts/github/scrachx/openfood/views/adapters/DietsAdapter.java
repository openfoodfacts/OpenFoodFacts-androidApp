package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.EditDietFragment;
import openfoodfacts.github.scrachx.openfood.fragments.DietsFragment;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;

public class DietsAdapter extends RecyclerView.Adapter<DietsAdapter.ViewHolder> {

    private List<Diet> mDiets;
    private final DietsFragment.ClickListener listener;
    private String languageCode = Locale.getDefault().getLanguage();

    public DietsAdapter(List<Diet> diets, DietsFragment.ClickListener listener) {
        mDiets = diets;
        this.listener = listener;
    }

    public void setDiets(List<Diet> diets) {
        mDiets = diets;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView nameTextView;
        //public Button messageButton;
        public Switch dietEnabledSwitch;
        private WeakReference<DietsFragment.ClickListener> listenerRef;

        public ViewHolder(View itemView, DietsFragment.ClickListener listener) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.diet_name);
            dietEnabledSwitch = itemView.findViewById(R.id.diet_enabled);
            //messageButton = itemView.findViewById(R.id.delete_button);
            listenerRef = new WeakReference<>(listener);
            //itemView.setOnClickListener(this);
            nameTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listenerRef.get().onPositionClicked(getAdapterPosition(), view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dietView = inflater.inflate(R.layout.item_diet, parent, false);
        return new ViewHolder(dietView, listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Diet diet = mDiets.get(position);
        TextView textView = holder.nameTextView;
        IDietRepository dietRepository = DietRepository.getInstance();
        DietName dietName = dietRepository.getDietNameByDietTagAndLanguageCode(diet.getTag(), languageCode);
        textView.setText(dietName.getName());
        Switch mSwitch = holder.dietEnabledSwitch;
        mSwitch.setChecked(diet.getEnabled());
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IDietRepository dietRepository = DietRepository.getInstance();
                dietRepository.setDietEnabled(diet.getTag(), isChecked);
            }
        });
        textView.setTag(textView.getText());
    }

    @Override
    public int getItemCount() {
        if (mDiets == null) {
            mDiets = new ArrayList<Diet>();
        }
        return mDiets.size();
    }

    /**
     * Modify a diet.
     */
    @OnClick
    public void onClick() {
    }

    void openFragmentEditDietForModification () {
        Fragment fragment = new EditDietFragment();
        FragmentTransaction transaction = fragment.getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment );
        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
        transaction.commit();
    }

}