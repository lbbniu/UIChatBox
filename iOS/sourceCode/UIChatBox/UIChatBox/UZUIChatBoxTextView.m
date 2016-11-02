/**
  * APICloud Modules
  * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
  * Licensed under the terms of the The MIT License (MIT).
  * Please see the license.html included with this distribution for details.
  */

#import "UZUIChatBoxTextView.h"

@implementation UZUIChatBoxTextView

@synthesize placeholder;

- (void)dealloc{
    if (placeholder) {
        [placeholder removeFromSuperview];
        self.placeholder = nil;
    }
}

- (id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
        //标签
        UILabel *markLabel = [[UILabel alloc]init];
        markLabel.frame = CGRectMake(10, 1, frame.size.width-30, frame.size.height);
        markLabel.backgroundColor = [UIColor clearColor];
        markLabel.textColor = [UIColor grayColor];
        markLabel.textAlignment = NSTextAlignmentLeft;
        markLabel.userInteractionEnabled = NO;
        [self addSubview:markLabel];
        self.placeholder = markLabel;
        self.textContainerInset = UIEdgeInsetsMake(8,8,0,28);
    }
    return self;
}

@end
