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
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.marker.util.ExtractorMail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GMailContacts extends AbstractActivityContacts {
    private static final String[] SCOPES = { GmailScopes.GMAIL_READONLY };

    @Override
    protected AsyncTask<Void, Void, List<String>> crearRequest() {
        return new MakeRequestTask(mCredential);
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
        private Gmail mService = null;
        private Exception mLastError = null;
        private int cantMensajes;
        private int progreso;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
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
            // Get the mails in the user's account.
            String user = "me";
            Set<String> mails = new HashSet<>();
            ListLabelsResponse listResponse =
                    mService.users().labels().list(user).execute();
            ListMessagesResponse messagesResponse = mService.users()
                    .messages()
                    .list(user)
                    .setMaxResults(22L)
                    .execute();
            List<String> metadataHeaders = Collections
                    .singletonList("from");
            ExtractorMail extractor = new ExtractorMail();
            List<Message> messages = messagesResponse.getMessages();
            cantMensajes = messages.size();
            for (int i = 0; i < cantMensajes; i++) {
                Message message = messages.get(i);
                if (message == null) {
                    continue;
                }

                Message execute = mService.users()
                        .messages()
                        .get(user, message.getId())
                        .setFormat("metadata")
                        .setMetadataHeaders(metadataHeaders)
                        .setFields("payload")
                        .execute();

                MessagePart payload = execute.getPayload();
                if (payload == null) {
                    continue;
                }
                List<MessagePartHeader> headers = payload.getHeaders();
                String mail = headers.get(0).getValue();

                //Se extrae el mail de entre los < >
                mail = extractor.extraer(mail);

                if (mail.contains("no-reply") ||
                        mail.contains("noreply")) {
                    //mensajes automaticos
                    continue;
                }
                if (mail.contains("+")) {
                    //cosas raras
                    continue;
                }
                mails.add(mail);
                progreso = i;
                publishProgress();
            }

            return new ArrayList<>(mails);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mProgress.setMax(cantMensajes);
            mProgress.setProgress(progreso);
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.setProgress(0);
            mProgressContainer.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgressContainer.setVisibility(View.GONE);
            if (output == null || output.size() == 0) {
                mOutputText.setText("Sin resultados");
            } else {
                output.add(0, "Contactos conseguidos usando Gmail API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgressContainer.setVisibility(View.GONE);
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
                    mOutputText.setText("Error:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Peticion cancelada.");
            }
        }
    }
}
