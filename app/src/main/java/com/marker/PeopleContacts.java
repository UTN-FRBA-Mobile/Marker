package com.marker;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeopleContacts extends AbstractActivityContacts {

    private static final String[] SCOPES = {PeopleServiceScopes.CONTACTS_READONLY};

    @Override
    protected AsyncTask<Void, Void, List<String>> crearRequest() {
        return new PeopleContacts.MakeRequestTask(mCredential);
    }

    @Override
    protected String[] getScope() {
        return SCOPES;
    }

    /**
     * An asynchronous task that handles the Gmail API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private PeopleService mService = null;
        private Exception mLastError = null;

        MakeRequestTask(final GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new PeopleService.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Marker")
                    .build();
        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get the labels in the user's account.
            String user = "me";
            List<String> labels = new ArrayList<>();
            ListConnectionsResponse response = mService.people().connections().list("people/me")
                    .setPersonFields("names,emailAddresses")
                    .execute();

            List<Person> connections = response.getConnections();

            for (Person label : connections) {
                String item = "";
                List<Name> names = label.getNames();
                if (names != null) {
                    for (Name name : names) {
                        item += " " + name.getDisplayName();
                    }
                }
                List<EmailAddress> emailAddresses = label.getEmailAddresses();
                if (emailAddresses != null) {
                    for (EmailAddress email : emailAddresses) {
                        item += " " + email.get("value");
                    }
                }
                if (item.isEmpty()) {
                    continue;
                }
                labels.add(item);
            }
            return labels;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.setVisibility(View.GONE);
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Gmail API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.setVisibility(View.GONE);
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
