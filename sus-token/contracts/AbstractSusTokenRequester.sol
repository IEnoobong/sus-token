// SPDX-License-Identifier: MIT
pragma solidity 0.8.20;

import "./StructData.sol";

interface AbstractSusTokenRequester {

    function makeRequest(address _walletAddress, uint256 _lastKnownNonce) external;

    function getWalletPoints(address _userAddress) external view returns (ApiResponse memory);

}
