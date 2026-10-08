package com.example.data.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

data class SimCard(
    val slotIndex: Int,          // 1 for SIM 1, 2 for SIM 2 (1-based for UI display)
    val subscriptionId: Int,     // Real system subscription ID (-1 if simulated or invalid)
    val displayName: String,     // e.g. "SIM 1", "Personal", "Work", or carrier name
    val carrierName: String,     // e.g. "Verizon", "T-Mobile", "AT&T", "Jio", "Vodafone"
    val phoneNumber: String? = null,
    val isDefaultSms: Boolean = false,
    val isSystemDetected: Boolean = false
) {
    val fullLabel: String
        get() {
            val carrier = carrierName.trim()
            val name = displayName.trim()
            val suffix = if (isDefaultSms) " (Default)" else ""
            return when {
                carrier.isNotBlank() && name.isNotBlank() && carrier != name -> "SIM $slotIndex • $name ($carrier)$suffix"
                carrier.isNotBlank() -> "SIM $slotIndex • $carrier$suffix"
                name.isNotBlank() && name != "SIM $slotIndex" -> "SIM $slotIndex • $name$suffix"
                else -> "SIM $slotIndex$suffix"
            }
        }

    val shortLabel: String
        get() {
            val carrier = carrierName.trim()
            val name = displayName.trim()
            return when {
                name.isNotBlank() && name != "SIM $slotIndex" -> name
                carrier.isNotBlank() -> carrier
                else -> "SIM $slotIndex"
            }
        }

    val badgeLabel: String
        get() = "SIM $slotIndex"

    companion object {
        val DEFAULT_DUAL_SIMS = listOf(
            SimCard(
                slotIndex = 1,
                subscriptionId = 1,
                displayName = "SIM 1",
                carrierName = "Carrier 1",
                isDefaultSms = true,
                isSystemDetected = false
            ),
            SimCard(
                slotIndex = 2,
                subscriptionId = 2,
                displayName = "SIM 2",
                carrierName = "Carrier 2",
                isDefaultSms = false,
                isSystemDetected = false
            )
        )

        fun getAvailableSims(context: Context): List<SimCard> {
            val subManager = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.getSystemService(SubscriptionManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SubscriptionManager.from(context)
                }
            } catch (e: Exception) {
                null
            }

            val defaultSmsSubId = try {
                SubscriptionManager.getDefaultSmsSubscriptionId()
            } catch (e: Exception) {
                SubscriptionManager.INVALID_SUBSCRIPTION_ID
            }

            val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (subManager != null && hasPhoneStatePermission) {
                try {
                    val activeSubs: List<SubscriptionInfo>? = subManager.activeSubscriptionInfoList
                    if (!activeSubs.isNullOrEmpty()) {
                        val sortedSubs = activeSubs.sortedBy { it.simSlotIndex }
                        val detected = sortedSubs.mapIndexed { index, sub ->
                            val slot = if (sub.simSlotIndex >= 0) sub.simSlotIndex + 1 else index + 1
                            val rawName = sub.displayName?.toString()?.trim() ?: ""
                            val carrier = sub.carrierName?.toString()?.trim() ?: ""
                            val isDefault = (sub.subscriptionId == defaultSmsSubId) || (index == 0 && defaultSmsSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID)

                            val phoneNum = try {
                                sub.number?.takeIf { it.isNotBlank() }
                            } catch (e: Exception) {
                                null
                            }

                            val resolvedName = when {
                                rawName.isNotBlank() && rawName != "CARD $slot" -> rawName
                                carrier.isNotBlank() -> carrier
                                else -> "SIM $slot"
                            }

                            SimCard(
                                slotIndex = slot,
                                subscriptionId = sub.subscriptionId,
                                displayName = resolvedName,
                                carrierName = carrier.ifBlank { resolvedName },
                                phoneNumber = phoneNum,
                                isDefaultSms = isDefault,
                                isSystemDetected = true
                            )
                        }

                        if (detected.size >= 2) {
                            return detected
                        } else if (detected.size == 1) {
                            val sim1 = detected[0]
                            val sim2Slot = if (sim1.slotIndex == 1) 2 else 1
                            val sim2 = SimCard(
                                slotIndex = sim2Slot,
                                subscriptionId = if (sim1.subscriptionId == 1) 2 else 1,
                                displayName = "SIM $sim2Slot",
                                carrierName = "Secondary / Work",
                                isDefaultSms = false,
                                isSystemDetected = false
                            )
                            return listOf(sim1, sim2).sortedBy { it.slotIndex }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.w("SimCard", "SecurityException checking active subscriptions: ${e.message}")
                } catch (e: Exception) {
                    Log.w("SimCard", "Error reading active subscriptions: ${e.message}")
                }
            }

            // Fallback: check TelephonyManager for primary carrier
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val opName = telephonyManager?.simOperatorName?.takeIf { it.isNotBlank() }
                    ?: telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() }
                    ?: "Primary SIM"

                return listOf(
                    SimCard(
                        slotIndex = 1,
                        subscriptionId = if (defaultSmsSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) defaultSmsSubId else 1,
                        displayName = "SIM 1",
                        carrierName = opName,
                        isDefaultSms = true,
                        isSystemDetected = false
                    ),
                    SimCard(
                        slotIndex = 2,
                        subscriptionId = 2,
                        displayName = "SIM 2",
                        carrierName = "Work / Secondary",
                        isDefaultSms = false,
                        isSystemDetected = false
                    )
                )
            } catch (e: Exception) {
                return DEFAULT_DUAL_SIMS
            }
        }

        fun getDefaultSimSlot(context: Context): Int {
            val sims = getAvailableSims(context)
            val defaultSim = sims.find { it.isDefaultSms }
            return defaultSim?.slotIndex ?: sims.firstOrNull()?.slotIndex ?: 1
        }
    }
}
