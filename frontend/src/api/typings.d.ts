declare namespace API {
  type BusinessResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BusinessResponseImage_ = {
    code?: number
    data?: Image
    message?: string
  }

  type BusinessResponseImageExpandTaskCreateResult_ = {
    code?: number
    data?: ImageExpandTaskCreateResult
    message?: string
  }

  type BusinessResponseImageExpandTaskStatusQueryResult_ = {
    code?: number
    data?: ImageExpandTaskStatusQueryResult
    message?: string
  }

  type BusinessResponseImageTagCategory_ = {
    code?: number
    data?: ImageTagCategory
    message?: string
  }

  type BusinessResponseImageVO_ = {
    code?: number
    data?: ImageVO
    message?: string
  }

  type BusinessResponseInt_ = {
    code?: number
    data?: number
    message?: string
  }

  type BusinessResponseListSpaceLevelVO_ = {
    code?: number
    data?: SpaceLevelVO[]
    message?: string
  }

  type BusinessResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BusinessResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BusinessResponsePageImage_ = {
    code?: number
    data?: PageImage_
    message?: string
  }

  type BusinessResponsePageImageVO_ = {
    code?: number
    data?: PageImageVO_
    message?: string
  }

  type BusinessResponsePageSpace_ = {
    code?: number
    data?: PageSpace_
    message?: string
  }

  type BusinessResponsePageSpaceVO_ = {
    code?: number
    data?: PageSpaceVO_
    message?: string
  }

  type BusinessResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BusinessResponseSpace_ = {
    code?: number
    data?: Space
    message?: string
  }

  type BusinessResponseSpaceVO_ = {
    code?: number
    data?: SpaceVO
    message?: string
  }

  type BusinessResponseString_ = {
    code?: number
    data?: string
    message?: string
  }

  type BusinessResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BusinessResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type BusinessResponseVoid_ = {
    code?: number
    message?: string
  }

  type DeleteRequest = {
    id?: string
  }

  type downloadFileUsingGETParams = {
    /** filePath */
    filePath: string
  }

  type getImageByIdUsingGETParams = {
    /** id */
    id: number
  }

  type getImageVOByIdUsingGETParams = {
    /** id */
    id: string
  }

  type getSpaceByIdUsingGETParams = {
    /** id */
    id: number
  }

  type getSpaceVOByIdUsingGETParams = {
    /** id */
    id: string
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id: number
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id: number
  }

  type Image = {
    createTime?: string
    deleted?: number
    id?: number
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: number
    imageTags?: string
    imageUrl?: string
    imageWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    spaceId?: string
    thumbnailUrl?: string
    updateTime?: string
    userId?: number
  }

  type ImageBatchUpdateRequest = {
    imageCategory?: string
    imageIds?: string[]
    imageTagList?: string[]
    nameTemplate?: string
    spaceId?: string
  }

  type ImageExpandRequest = {
    id?: string
    parameters?: Parameters
  }

  type ImageExpandTaskCreateResult = {
    code?: string
    message?: string
    output?: Output
    requestId?: string
  }

  type ImageExpandTaskStatusQueryResult = {
    output?: Output1
    requestId?: string
  }

  type ImageFetchRequest = {
    fetchSize?: number
    imageNamePrefix?: string
    searchText?: string
  }

  type ImageQueryRequest = {
    current?: number
    id?: number
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: number
    imageTagList?: string[]
    imageWidth?: number
    keyword?: string
    pageSize?: number
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    sortField?: string
    sortOrder?: string
    spaceId?: string
    updateTimeEnd?: string
    updateTimeStart?: string
    userId?: number
  }

  type ImageReviewRequest = {
    current?: number
    id?: number
    pageSize?: number
    reviewMessage?: string
    reviewStatus?: number
    sortField?: string
    sortOrder?: string
  }

  type ImageTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type ImageUpdateRequest = {
    id?: number
    imageCategory?: string
    imageIntro?: string
    imageName?: string
    imageTagList?: string[]
  }

  type ImageUploadRequest = {
    fileUrl?: string
    id?: string
    imageName?: string
    spaceId?: string
  }

  type ImageVO = {
    createTime?: string
    id?: number
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: number
    imageTagList?: string[]
    imageUrl?: string
    imageWidth?: number
    spaceId?: string
    thumbnailUrl?: string
    updateTime?: string
    userId?: number
    userVO?: UserVO
  }

  type LoginUserVO = {
    createTime?: string
    id?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type Output = {
    taskId?: string
    taskStatus?: string
  }

  type Output1 = {
    code?: string
    endTime?: string
    message?: string
    outputImageUrl?: string
    scheduledTime?: string
    submitTime?: string
    taskId?: string
    taskMetrics?: TaskMetrics
    taskStatus?: string
  }

  type PageImage_ = {
    current?: number
    pages?: number
    records?: Image[]
    size?: number
    total?: number
  }

  type PageImageVO_ = {
    current?: number
    pages?: number
    records?: ImageVO[]
    size?: number
    total?: number
  }

  type PageSpace_ = {
    current?: number
    pages?: number
    records?: Space[]
    size?: number
    total?: number
  }

  type PageSpaceVO_ = {
    current?: number
    pages?: number
    records?: SpaceVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type Parameters = {
    addWatermark?: boolean
    angle?: number
    bestQuality?: boolean
    bottomOffset?: number
    leftOffset?: number
    limitImageSize?: boolean
    outputRatio?: string
    rightOffset?: number
    topOffset?: number
    xScale?: number
    yScale?: number
  }

  type queryImageExpandTaskStatusUsingGETParams = {
    /** taskId */
    taskId: string
  }

  type Space = {
    createTime?: string
    currentCount?: number
    currentSize?: number
    deleted?: number
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    updateTime?: string
    userId?: number
  }

  type SpaceAddRequest = {
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceLevelVO = {
    baseMaxCount?: number
    baseMaxSize?: number
    desc?: string
    value?: number
  }

  type SpaceQueryRequest = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    userId?: number
  }

  type SpaceUpdateRequest = {
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceVO = {
    createTime?: string
    currentCount?: number
    currentSize?: number
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    updateTime?: string
    userId?: number
    userVO?: UserVO
  }

  type TaskMetrics = {
    failed?: number
    succeeded?: number
    total?: number
  }

  type uploadImageUsingPOSTParams = {
    fileUrl?: string
    id?: number
    imageName?: string
    spaceId?: string
  }

  type User = {
    createTime?: string
    deleted?: number
    id?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserAddRequest = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: number
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
