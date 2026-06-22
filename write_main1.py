# Helper to write MainScreen.kt
n = chr(10)
code = (
    'package com.xiaobaotv.app.ui.navigation' + n +
    '' + n +
    'import androidx.compose.foundation.layout.padding' + n +
    'import androidx.compose.material3.Icon' + n +
    'import androidx.compose.material3.NavigationBar' + n +
    'import androidx.compose.material3.NavigationBarItem' + n +
    'import androidx.compose.material3.Scaffold' + n +
    'import androidx.compose.material3.Text' + n +
    'import androidx.compose.runtime.Composable' + n +
    'import androidx.compose.runtime.getValue' + n +
    'import androidx.compose.ui.Modifier' + n +
    'import androidx.navigation.NavGraph.Companion.findStartDestination' + n +
    'import androidx.navigation.compose.currentBackStackEntryAsState' + n +
    'import androidx.navigation.compose.rememberNavController' + n
)

path = 'D:/Projects/Xiaobao_Apk/app/src/main/java/com/xiaobaotv/app/ui/navigation/MainScreen.kt'
with open(path, 'w', encoding='utf-8') as f:
    f.write(code)
print('Part 1 done')
