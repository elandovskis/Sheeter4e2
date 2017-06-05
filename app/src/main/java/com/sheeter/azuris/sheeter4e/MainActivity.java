package com.sheeter.azuris.sheeter4e;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sheeter.azuris.sheeter4e.Modules.AbilityScores;
import com.sheeter.azuris.sheeter4e.Modules.D20Character;
import com.sheeter.azuris.sheeter4e.Modules.Details;
import com.sheeter.azuris.sheeter4e.Modules.Sheet;

import org.apache.commons.io.input.BOMInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private BottomNavigationView mNavigationView;
    private FragmentAdapter mFragmentAdapter;
    private ViewPager mViewPager;
    private D20Character mCharacter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = (ProgressBar) findViewById(R.id.Main_Progressbar);
        mViewPager = (ViewPager) findViewById(R.id.Main_Pager);
        mNavigationView = (BottomNavigationView) findViewById(R.id.Main_Navigation);
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_home)
                    mViewPager.setCurrentItem(0);
                else if (item.getItemId() == R.id.action_powers)
                    mViewPager.setCurrentItem(1);
                return true;
            }
        });

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mFragmentAdapter =
                new FragmentAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.Main_Pager);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        if (position == 0) {
                            mNavigationView.setSelectedItemId(R.id.action_home);
                        }
                        else if (position == 1) {
                            mNavigationView.setSelectedItemId(R.id.action_powers);
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            parseXMLFile();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                checkFilePerms();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkFilePerms() {
        mProgressBar.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 0);
            } else {
                parseXMLFile();
            }
        }
    }

    private void parseXMLFile() {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/Jak.dnd4e");
            BOMInputStream fin = new BOMInputStream(new FileInputStream(file));
            String contents = convertStreamToString(fin);
            fin.close();

            String textState = "";
            String tagState = "";

            xpp.setInput(new StringReader(contents));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag "+xpp.getName());

                    String tagName = xpp.getName();
                    // Switch on tag name
                    switch (tagName) {
                        case "D20Character":
                            mCharacter = new D20Character(xpp.getAttributeValue(2));
                            break;
                        case "CharacterSheet":
                            mCharacter.setSheet(new Sheet());
                            break;
                        case "Details":
                            tagState = tagName;
                            mCharacter.sheet.setDetails(new Details());
                            break;
                        case "AbilityScores":
                            mCharacter.sheet.setAbilityScores(new AbilityScores());
                            break;
                        case "Strength":
                            mCharacter.sheet.abilityScores.setStrength(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "Constitution":
                            mCharacter.sheet.abilityScores.setConstitution(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "Dexterity":
                            mCharacter.sheet.abilityScores.setDexterity(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "Intelligence":
                            mCharacter.sheet.abilityScores.setIntelligence(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "Wisdom":
                            mCharacter.sheet.abilityScores.setWisdom(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "Charisma":
                            mCharacter.sheet.abilityScores.setCharisma(Integer.parseInt(xpp.getAttributeValue(0).trim()));
                            break;
                        case "StatBlock":
                            mCharacter.sheet.stats = new ArrayList<>();
                            break;
                        case "Stat":
                            StatParse(xpp, mCharacter);
                            break;
                        default:
                            textState = tagName;
                            break;
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    System.out.println("End tag "+xpp.getName());

                    String tagName = xpp.getName();
                    // Switch on tag name
                    switch (tagName) {
                        case "Details":
                        case "AbilityScores":
                            tagState = "";
                            break;
                    }

                    textState = "";
                } else if(eventType == XmlPullParser.TEXT) {
                    System.out.println("Text "+xpp.getText());

                    String text = xpp.getText().trim();

                    if (tagState.equals("Details")) {
                        // Switch on tag text for details
                        switch (textState) {
                            case "name":
                                mCharacter.sheet.details.setName(text);
                                break;
                            case "Level":
                                mCharacter.sheet.details.setLevel(Integer.parseInt(text));
                                break;
                            case "Player":
                                mCharacter.sheet.details.setPlayer(text);
                                break;
                            case "Height":
                                mCharacter.sheet.details.setHeight(text);
                                break;
                            case "Weight":
                                mCharacter.sheet.details.setWeight(text);
                                break;
                            case "Gender":
                                mCharacter.sheet.details.setGender(text);
                                break;
                            case "Age":
                                mCharacter.sheet.details.setAge(Integer.parseInt(text));
                                break;
                            case "Alignment":
                                mCharacter.sheet.details.setAlignment(text);
                                break;
                            case "Company":
                                mCharacter.sheet.details.setCompany(text);
                                break;
                            case "Portrait":
                                mCharacter.sheet.details.setPortrait(text);
                                break;
                            case "Experience":
                                mCharacter.sheet.details.setExperience(Long.parseLong(text));
                                break;
                            case "CarriedMoney":
                                mCharacter.sheet.details.setCarriedMoney(text);
                                break;
                            case "StoredMoney":
                                mCharacter.sheet.details.setStoredMoney(text);
                                break;
                        }
                    }
                }
                eventType = xpp.next();
            }
            System.out.println("End document");
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateView();
    }

    private void updateView() {
        View mainPage = mViewPager.getChildAt(0);

        ((TextView) mainPage.findViewById(R.id.Main_TextView_Character)).setText(String.format(Locale.CANADA,"Level %d %s", mCharacter.sheet.details.getLevel(), mCharacter.sheet.details.getName()));
        ((TextView) mainPage.findViewById(R.id.Score_Strength)).setText(String.valueOf(mCharacter.sheet.abilityScores.getStrength()));
        ((TextView) mainPage.findViewById(R.id.Score_Constitution)).setText(String.valueOf(mCharacter.sheet.abilityScores.getConstitution()));
        ((TextView) mainPage.findViewById(R.id.Score_Dexterity)).setText(String.valueOf(mCharacter.sheet.abilityScores.getDexterity()));
        ((TextView) mainPage.findViewById(R.id.Score_Intelligence)).setText(String.valueOf(mCharacter.sheet.abilityScores.getIntelligence()));
        ((TextView) mainPage.findViewById(R.id.Score_Wisdom)).setText(String.valueOf(mCharacter.sheet.abilityScores.getWisdom()));
        ((TextView) mainPage.findViewById(R.id.Score_Charisma)).setText(String.valueOf(mCharacter.sheet.abilityScores.getCharisma()));

        mProgressBar.setVisibility(View.GONE);
    }

    private void StatParse(XmlPullParser xpp, D20Character character) {
        // TODO: parse each stat
        String statName = xpp.getAttributeValue(0);

        switch (statName){
            case "Strength":
                mCharacter.sheet.abilityScores.setStrength(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            case "Constitution":
                mCharacter.sheet.abilityScores.setConstitution(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            case "Dexterity":
                mCharacter.sheet.abilityScores.setDexterity(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            case "Intelligence":
                mCharacter.sheet.abilityScores.setIntelligence(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            case "Wisdom":
                mCharacter.sheet.abilityScores.setWisdom(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            case "Charisma":
                mCharacter.sheet.abilityScores.setCharisma(Integer.parseInt(xpp.getAttributeValue(1).trim()));
                break;
            default:
                ;
        }
        int a = 123;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    private class FragmentAdapter extends FragmentStatePagerAdapter {
        Fragment[] fragments;

        FragmentAdapter(FragmentManager fm) {
            super(fm);
            fragments = new Fragment[2];
            fragments[0] = new MainFragment();
            fragments[1] = new AddSheetsFragment();
        }

        @Override
        public Fragment getItem(int i) {
            return fragments[i];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
