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
    data?: string
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
    /** 文件路径 */
    filePath: string
  }

  type getImageByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type getImageVOByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type getSpaceByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type getSpaceVOByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type getUserByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type getUserVOByIdUsingGETParams = {
    /** 编号 */
    id: string
  }

  type Image = {
    createTime?: string
    deleted?: number
    id?: string
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: string
    imageTags?: string
    imageUrl?: string
    imageWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: string
    spaceId?: string
    thumbnailUrl?: string
    updateTime?: string
    userId?: string
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
    id?: string
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: string
    imageTagList?: string[]
    imageWidth?: number
    keyword?: string
    pageSize?: number
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: string
    sortField?: string
    sortOrder?: string
    spaceId?: string
    updateTimeEnd?: string
    updateTimeStart?: string
    userId?: string
  }

  type ImageReviewRequest = {
    current?: number
    id?: string
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
    id?: string
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
    id?: string
    imageCategory?: string
    imageColor?: string
    imageFormat?: string
    imageHeight?: number
    imageIntro?: string
    imageName?: string
    imageScale?: number
    imageSize?: string
    imageTagList?: string[]
    imageUrl?: string
    imageWidth?: number
    spaceId?: string
    thumbnailUrl?: string
    updateTime?: string
    userId?: string
    userVO?: UserVO
  }

  type LoginUserVO = {
    createTime?: string
    id?: string
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
    current?: string
    pages?: string
    records?: Image[]
    size?: string
    total?: string
  }

  type PageImageVO_ = {
    current?: string
    pages?: string
    records?: ImageVO[]
    size?: string
    total?: string
  }

  type PageSpace_ = {
    current?: string
    pages?: string
    records?: Space[]
    size?: string
    total?: string
  }

  type PageSpaceVO_ = {
    current?: string
    pages?: string
    records?: SpaceVO[]
    size?: string
    total?: string
  }

  type PageUserVO_ = {
    current?: string
    pages?: string
    records?: UserVO[]
    size?: string
    total?: string
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
    /** 任务编号 */
    taskId: string
  }

  type Space = {
    createTime?: string
    currentCount?: string
    currentSize?: string
    deleted?: number
    id?: string
    maxCount?: string
    maxSize?: string
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    updateTime?: string
    userId?: string
  }

  type SpaceAddRequest = {
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
  }

  type SpaceLevelVO = {
    baseMaxCount?: string
    baseMaxSize?: string
    desc?: string
    value?: number
  }

  type SpaceQueryRequest = {
    current?: number
    id?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    userId?: string
  }

  type SpaceUpdateRequest = {
    id?: string
    maxCount?: string
    maxSize?: string
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceVO = {
    createTime?: string
    currentCount?: string
    currentSize?: string
    id?: string
    maxCount?: string
    maxSize?: string
    spaceLevel?: number
    spaceName?: string
    updateTime?: string
    userId?: string
    userVO?: UserVO
  }

  type TaskMetrics = {
    failed?: number
    succeeded?: number
    total?: number
  }

  type uploadImageUsingPOSTParams = {
    fileUrl?: string
    id?: string
    imageName?: string
    spaceId?: string
  }

  type User = {
    createTime?: string
    deleted?: number
    id?: string
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
    id?: string
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
    id?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
