import { View, Text } from '@tarojs/components'

export default function DealerIndex() {
  return (
    <View className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <Text className="text-xl font-semibold text-green-600">经销商端首页</Text>
      <Text className="mt-2 text-sm text-gray-500">欢迎回来，经销商</Text>
    </View>
  )
}
