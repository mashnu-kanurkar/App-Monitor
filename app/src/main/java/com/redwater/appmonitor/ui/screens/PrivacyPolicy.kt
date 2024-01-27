package com.redwater.appmonitor.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redwater.appmonitor.Constants
import com.redwater.appmonitor.R
import com.redwater.appmonitor.data.UserPreferences
import com.redwater.appmonitor.data.dataStore
import com.redwater.appmonitor.ui.components.AnnotatedClickableText

@Composable
fun PrivacyPolicyAgreementScreen(onClickAgree: ()-> Unit) {
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 0.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.privacy_policy_title), style = MaterialTheme.typography.titleLarge)
            Text(modifier = Modifier.padding(4.dp),text = stringResource(id = R.string.privacy_policy_description))
            Spacer(modifier = Modifier.height(48.dp))
            AnnotatedClickableText(normalTextPart = stringResource(id = R.string.privacy_policy_url_holder).replace("##privacy_policy##", " "),
                clickableTextPart = "Privacy Policy", url = Constants.privacyPolicyURL)
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onClickAgree) {
                Text(modifier = Modifier.padding(16.dp, 4.dp), text = stringResource(id = R.string.button_accept))
            }
        }
    }
}