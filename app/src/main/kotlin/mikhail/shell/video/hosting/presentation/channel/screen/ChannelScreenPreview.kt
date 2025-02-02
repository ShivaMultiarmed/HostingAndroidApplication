package mikhail.shell.video.hosting.presentation.channel.screen

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mikhail.shell.video.hosting.di.ApiModule
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.SubscriptionState.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.channel.screen.sections.ChannelHeaderShrinked
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

val videosSamples: List<Video> = ApiModule.provideGson().fromJson("""
    [
    {
        "videoId": 38,
        "channelId": 1,
        "title": "funny conversation on some phone",
        "dateTime": "2024-12-28T22:48:46",
        "views": 135,
        "likes": 2,
        "dislikes": 0,
        "sourceUrl": "http://192.168.1.2:9999/api/v1/videos/38/play",
        "coverUrl": "http://192.168.1.2:9999/api/v1/videos/38/cover"
    },
    {
        "videoId": 6,
        "channelId": 1,
        "title": "Master Yoda vs Count Dooku",
        "dateTime": "2024-12-12T16:40:49",
        "views": 6436357,
        "likes": 0,
        "dislikes": 1,
        "sourceUrl": "http://192.168.1.2:9999/api/v1/videos/6/play",
        "coverUrl": "http://192.168.1.2:9999/api/v1/videos/6/cover"
    },
    {
        "videoId": 1,
        "channelId": 1,
        "title": "Luke Skywalker vs Darth Vader - Final Battle",
        "dateTime": "2024-12-04T00:02:29",
        "views": 37,
        "likes": 2,
        "dislikes": 0,
        "sourceUrl": "http://192.168.1.2:9999/api/v1/videos/1/play",
        "coverUrl": "http://192.168.1.2:9999/api/v1/videos/1/cover"
    }
]
""".trimIndent(), object: TypeToken<List<Video>>() {}.type)

@Composable
@Preview
fun ChannelScreenPreview() {
    VideoHostingTheme {
        ChannelScreen(
            state = ChannelScreenState(
                channel = ChannelWithUser(
                    channelId = 100500,
                    ownerId = 200600,
                    title = "Популярные путешествия",
                    alias = "pop_travel",
                    description = "",
                    subscribers = 600,
                    subscription = NOT_SUBSCRIBED,
                    //coverUrl = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fzvetnoe.ru%2Fclub%2Fpoleznye-stati%2Fpeyzazhnaya-zhivopis-istoriya-osnovnye-vidy-i-stili%2F&psig=AOvVaw3xFPUBEjt0Mr7ObFtKhEGm&ust=1738573732144000&source=images&cd=vfe&opi=89978449&ved=0CBQQjRxqFwoTCJim8YDSpIsDFQAAAAAdAAAAABAE",
                    //avatarUrl = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.istockphoto.com%2Fphotos%2Fpeople-travelling&psig=AOvVaw3IXuhTWowQb33rawwJGVq1&ust=1738573780538000&source=images&cd=vfe&opi=89978449&ved=0CBQQjRxqFwoTCOiLt5jSpIsDFQAAAAAdAAAAABAE"
                ),
                videos = videosSamples
            ),
            onChannelRefresh = {},
            onVideosRefresh = {},
            onSubscription = {},
            onVideoClick = {}
        )
    }
}

@Composable
@Preview
fun ChannelCoverShrinkedPreview() {
    VideoHostingTheme {
        ChannelScreen(
            state = ChannelScreenState(
                channel = ChannelWithUser(
                    channelId = 100500,
                    ownerId = 200600,
                    title = "Популярные путешествия",
                    alias = "pop_travel",
                    description = "",
                    subscribers = 600,
                    subscription = NOT_SUBSCRIBED,
                    //coverUrl = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fzvetnoe.ru%2Fclub%2Fpoleznye-stati%2Fpeyzazhnaya-zhivopis-istoriya-osnovnye-vidy-i-stili%2F&psig=AOvVaw3xFPUBEjt0Mr7ObFtKhEGm&ust=1738573732144000&source=images&cd=vfe&opi=89978449&ved=0CBQQjRxqFwoTCJim8YDSpIsDFQAAAAAdAAAAABAE",
                    //avatarUrl = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.istockphoto.com%2Fphotos%2Fpeople-travelling&psig=AOvVaw3IXuhTWowQb33rawwJGVq1&ust=1738573780538000&source=images&cd=vfe&opi=89978449&ved=0CBQQjRxqFwoTCOiLt5jSpIsDFQAAAAAdAAAAABAE"
                ),
                videos = videosSamples
            ),
            onChannelRefresh = {},
            onVideosRefresh = {},
            onSubscription = {},
            onVideoClick = {}
        )
    }
}