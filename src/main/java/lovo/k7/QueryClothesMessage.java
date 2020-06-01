package lovo.k7;

import android.os.AsyncTask;

public class QueryClothesMessage extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        return new SameDatas().getData(params);
    }
}
