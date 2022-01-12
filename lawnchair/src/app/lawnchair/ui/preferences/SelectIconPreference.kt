package app.lawnchair.ui.preferences

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.lawnchair.icons.IconPickerItem
import app.lawnchair.ui.preferences.components.AppItem
import app.lawnchair.ui.preferences.components.PreferenceLayoutLazyColumn
import app.lawnchair.ui.preferences.components.preferenceGroupItems
import app.lawnchair.ui.util.OnResult
import com.android.launcher3.util.ComponentKey
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.selectIconGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{packageName}/{nameAndUser}"),
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("nameAndUser") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val packageName = args.getString("packageName")
            val nameAndUser = args.getString("nameAndUser")
            val key = ComponentKey.fromString("$packageName/$nameAndUser")!!
            SelectIconPreference(key)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun SelectIconPreference(componentKey: ComponentKey) {
    val context = LocalContext.current
    val label = remember(componentKey) {
        val launcherApps = context.getSystemService<LauncherApps>()!!
        val intent = Intent().setComponent(componentKey.componentName)
        val activity = launcherApps.resolveActivity(intent, componentKey.user)
        activity.label.toString()
    }
    val iconPacks by LocalPreferenceInteractor.current.iconPacks.collectAsState()
    val navController = LocalNavController.current

    OnResult<IconPickerItem> {
        Log.d("SelectIconPreference", "result: $it")
        (context as Activity).finish()
    }

    PreferenceLayoutLazyColumn(label = label) {
        preferenceGroupItems(
            heading = { "Choose icon from" },
            items = iconPacks,
            isFirstChild = true
        ) { _, iconPack ->
            AppItem(
                label = iconPack.name,
                icon = remember(iconPack) { iconPack.icon.toBitmap() },
                onClick = { navController.navigate("/${Routes.ICON_PICKER}/${iconPack.packageName}/") }
            )
        }
    }
}
