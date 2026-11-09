package com.npplayer.player.ui.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow

@Composable
fun ResumeQuestionCard(
    visible: Boolean,
    formattedTime: String,
    onResume: (rememberChoice: Boolean) -> Unit,
    onStartOver: (rememberChoice: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var rememberChoice by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(visible) {
        if (visible) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NPBlack.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside card so it doesn't leak */ }
                    }
                    .semantics { contentDescription = "Resume playback question: Continue from $formattedTime?" }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Continue from $formattedTime?",
                        color = NPWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // START OVER (outlined white text on black, min 48dp)
                        OutlinedButton(
                            onClick = { onStartOver(rememberChoice) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NPWhite.copy(alpha = 0.40f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = NPWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics { contentDescription = "Start over from beginning" }
                        ) {
                            Text(
                                text = "START OVER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NPWhite,
                                maxLines = 1
                            )
                        }

                        // RESUME (yellow, black text, min 48dp)
                        Button(
                            onClick = { onResume(rememberChoice) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NPYellow,
                                contentColor = NPBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics { contentDescription = "Resume from $formattedTime" }
                        ) {
                            Text(
                                text = "RESUME",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NPBlack,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remember my choice checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { rememberChoice = !rememberChoice }
                            .padding(vertical = 4.dp)
                            .semantics { contentDescription = "Remember my choice checkbox" }
                    ) {
                        Checkbox(
                            checked = rememberChoice,
                            onCheckedChange = { rememberChoice = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NPYellow,
                                checkmarkColor = NPBlack,
                                uncheckedColor = NPWhite.copy(alpha = 0.50f)
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remember my choice",
                            color = NPWhite.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
