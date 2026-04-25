// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference types="@tarojs/taro" />

declare module '*.module.scss' {
  const classes: { readonly [key: string]: string }
  export default classes
}

/* eslint-disable @typescript-eslint/no-explicit-any */

declare module '@tarojs/taro' {
  import Taro from '@tarojs/taro/types/index'
  export default Taro
  export * from '@tarojs/taro/types/index'
}

declare module '@tarojs/components' {
  import { ComponentType, ReactNode } from 'react'

  interface ViewProps {
    className?: string
    style?: Record<string, string | number> | string
    children?: ReactNode
    onClick?: (event: any) => void
  }

  interface TextProps {
    className?: string
    children?: ReactNode
  }

  export const View: ComponentType<ViewProps>
  export const Text: ComponentType<TextProps>
  export const ScrollView: ComponentType<any>
  export const Swiper: ComponentType<any>
  export const Image: ComponentType<any>
  export const Input: ComponentType<any>
  export const Button: ComponentType<any>
  export const Navigator: ComponentType<any>
  export const Form: ComponentType<any>
}

declare namespace JSX {
  interface IntrinsicElements {
    [elemName: string]: any
  }
}

declare function defineAppConfig(config: {
  pages: string[]
  window?: Record<string, any>
  tabBar?: Record<string, any>
  subPackages?: any[]
  preloadRule?: Record<string, any>
  permission?: Record<string, any>
}): void

/* eslint-enable @typescript-eslint/no-explicit-any */

