package agh.mobile.blurfacesmcc.ui.navigation

import agh.mobile.blurfacesmcc.ui.homePage.HomePage
import agh.mobile.blurfacesmcc.ui.myvideos.MyVideosScreen
import agh.mobile.blurfacesmcc.ui.securitysettings.SecuritySettingsScreen
import agh.mobile.blurfacesmcc.ui.securitysettings.confidentialData.ConfidentialDataScreen
import agh.mobile.blurfacesmcc.ui.uploadvideo.UploadVideoScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun BlurFacesNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = BlurFacesDestinations.HOME_PAGE,
    showSnackbar: (String) -> Unit,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(
            BlurFacesDestinations.HOME_PAGE
        ) {
            HomePage(
                navigateToUploadVideo = navController::navigateToUploadVideo,
                navigateToVideos = navController::navigateToMyVideos,
                navigateToSecuritySettings = navController::navigateToSecuritySettings
            )
        }

        composable(
            BlurFacesDestinations.MY_VIDEOS
        ) {
            MyVideosScreen()
        }

        composable(
            BlurFacesDestinations.UPLOAD_VIDEO
        ) {
            UploadVideoScreen(
                navigateToHomePage = navController::navigateToHomePage,
                showSnackbar = showSnackbar
            )
        }

        composable(
            BlurFacesDestinations.SECURITY_SETTINGS
        ) {
            SecuritySettingsScreen(
                navigateToConfidentialData = navController::navigateToConfidentialData
            )
        }

        composable(
            BlurFacesDestinations.CONFIDENTIAL_DATA
        ) {
            ConfidentialDataScreen()
        }

    }

}

fun NavHostController.navigateSingleTopTo(destination: String) =
    navigate(destination){
        popUpTo(
            graph.findStartDestination().id,
        ) {
            inclusive = false
        }
        launchSingleTop = true
    }


fun NavHostController.navigateToHomePage() = navigateSingleTopTo(BlurFacesDestinations.HOME_PAGE)

fun NavHostController.navigateToUploadVideo() =
    navigateSingleTopTo(BlurFacesDestinations.UPLOAD_VIDEO)

fun NavHostController.navigateToMyVideos() = navigateSingleTopTo(BlurFacesDestinations.MY_VIDEOS)

fun NavHostController.navigateToSecuritySettings() =
    navigateSingleTopTo(BlurFacesDestinations.SECURITY_SETTINGS)

fun NavHostController.navigateToConfidentialData() =
    navigate(BlurFacesDestinations.CONFIDENTIAL_DATA)
