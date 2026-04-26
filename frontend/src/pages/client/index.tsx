import { View, Text } from '@tarojs/components'

export default function ClientIndex() {
  return (
    <View className="flex flex-col items-center justify-center min-h-screen bg-white">
      <Text className="text-xl font-semibold text-gray-800">客户端首页</Text>
      <Text className="mt-2 text-sm text-gray-400">欢迎来到商城</Text>
    </View>
  )
}
