package org.andre.strimzi.oauth.validator;

import org.andre.strimzi.oauth.common.TokenInfo;

public interface TokenValidator
{
    TokenInfo validate(String token);
}
