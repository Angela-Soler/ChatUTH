package com.proyecto.droidnotes.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

// METODO PARA UTLIZAR NUESTRO VIEWPAGER
// CABE MENCIONAR QUE LA DOCUMENTACION DE ANDROID LA COMPARTE Y SE PUEDE UTLIZAR EN CUALQUIER PROYECTO


// IMPORTAR METODOS QUE SON REQUERIDOS
public class ViewPagerAdapter extends FragmentPagerAdapter {


    // ARREGLOS
    List<Fragment> mFragmentList = new ArrayList<>();
    List<String> mFragmentTitleList = new ArrayList<>();

    // CONsTRUCTOR
    public ViewPagerAdapter(FragmentManager manager)
    {
        super(manager);
    }

    public void addFragment(Fragment fragment, String title)
    {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // RETORNAMOS LA POSICION QUE RECIBIMOS POR PARAMETRO
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        // RETORNAR EL TAMAÑO DE LA LISTA
        return mFragmentList.size();
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
