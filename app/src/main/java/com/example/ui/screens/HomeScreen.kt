package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.model.ContactRequest
import com.example.model.User
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassmorphismBorder
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMedium

@Composable
fun HomeScreen(
    currentUser: User,
    usersList: List<User>,
    myContacts: Set<String>,
    contactRequests: List<ContactRequest>,
    incomingCallSession: CallSession?,
    onOpenChat: (User) -> Unit,
    onOpenUserProfile: (User) -> Unit,
    onStartVoiceCall: (User) -> Unit,
    onStartVideoCall: (User) -> Unit,
    onAcceptIncomingCall: (CallSession) -> Unit,
    onDeclineIncomingCall: (CallSession) -> Unit,
    onSendContactRequest: (String) -> Unit,
    onAcceptContactRequest: (String, User?) -> Unit,
    onRemoveContact: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Contacts
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = RoyalBlueDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalBlueMedium)
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            photoUrl = currentUser.photoUrl,
                            displayName = currentUser.displayName,
                            isOnline = true,
                            size = 42.dp,
                            modifier = Modifier.clickable { onOpenProfile() }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "FaceTime",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = currentUser.displayName,
                                fontSize = 12.sp,
                                color = PlatinumGray
                            )
                        }
                    }

                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ElectricCyan
                        )
                    }
                }

                // Tab Row (Chats vs Contacts)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RoyalBlueMedium,
                    contentColor = PureWhite,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "CHATS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) ElectricCyan else PlatinumGray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CONTACTS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) ElectricCyan else PlatinumGray
                                )
                                if (contactRequests.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(CallRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${contactRequests.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (selectedTab == 0) "Search mutual chats..." else "Search email or user...",
                        color = PlatinumGray.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PlatinumGray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = RoyalBlueCard,
                    unfocusedContainerColor = RoyalBlueCard,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = GlassmorphismBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                singleLine = true
            )

            if (selectedTab == 0) {
                // TAB 0: CHATS (MUTUAL CONTACTS ONLY)
                val mutualUsers = usersList.filter {
                    myContacts.contains(it.uid) &&
                            (it.displayName.contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true))
                }

                if (mutualUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = PlatinumGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Mutual Chats Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = "Switch to 'Contacts' tab to add friends or accept pending requests.",
                                fontSize = 12.sp,
                                color = PlatinumGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mutualUsers) { contact ->
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOpenChat(contact) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        photoUrl = contact.photoUrl,
                                        displayName = contact.displayName,
                                        isOnline = contact.isOnline,
                                        size = 48.dp,
                                        modifier = Modifier.clickable { onOpenUserProfile(contact) }
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = contact.displayName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = contact.aboutStatus,
                                            fontSize = 13.sp,
                                            color = PlatinumGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row {
                                        IconButton(onClick = { onStartVoiceCall(contact) }) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Voice Call",
                                                tint = CallGreen
                                            )
                                        }
                                        IconButton(onClick = { onStartVideoCall(contact) }) {
                                            Icon(
                                                imageVector = Icons.Default.VideoCall,
                                                contentDescription = "Video Call",
                                                tint = ElectricCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: CONTACTS (PENDING REQUESTS & ALL USERS)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // PENDING CONTACT REQUESTS SECTION
                    if (contactRequests.isNotEmpty()) {
                        item {
                            Text(
                                text = "PENDING CONTACT REQUESTS (${contactRequests.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CallRed,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(contactRequests) { req ->
                            val senderUser = usersList.find { it.uid == req.senderId }
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = CallRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        photoUrl = req.senderPhoto,
                                        displayName = req.senderName,
                                        size = 44.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.senderName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = req.senderEmail,
                                            fontSize = 12.sp,
                                            color = PlatinumGray
                                        )
                                    }
                                    Button(
                                        onClick = { onAcceptContactRequest(req.senderId, senderUser) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CallGreen,
                                            contentColor = PureWhite
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Add Back", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // ALL USERS / DIRECTORY SECTION
                    item {
                        Text(
                            text = "DIRECTORY / SEARCH CONTACTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    val filteredUsers = usersList.filter {
                        it.displayName.contains(searchQuery, ignoreCase = true) ||
                                it.email.contains(searchQuery, ignoreCase = true)
                    }

                    items(filteredUsers) { target ->
                        val isMutual = myContacts.contains(target.uid)
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenUserProfile(target) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    photoUrl = target.photoUrl,
                                    displayName = target.displayName,
                                    isOnline = target.isOnline,
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = target.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = if (isMutual) "Mutual Contact ✅" else "Not Added Yet 🔒",
                                        fontSize = 12.sp,
                                        color = if (isMutual) CallGreen else PlatinumGray
                                    )
                                }

                                if (isMutual) {
                                    IconButton(onClick = { onRemoveContact(target.uid) }) {
                                        Icon(
                                            imageVector = Icons.Default.PersonRemove,
                                            contentDescription = "Remove Contact",
                                            tint = CallRed
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { onSendContactRequest(target.uid) }) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = "Add Contact",
                                            tint = ElectricCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
