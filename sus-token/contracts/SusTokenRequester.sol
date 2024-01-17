// SPDX-License-Identifier: MIT

pragma solidity 0.8.20;

import "@api3/airnode-protocol/contracts/rrp/requesters/RrpRequesterV0.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "./StructData.sol";

// A Requester that will return the requested data by calling the specified Airnode.
contract SusTokenRequester is RrpRequesterV0, Ownable {
    mapping(bytes32 => bool) public incomingFulfillments;
    mapping(address => ApiResponse) public fulfilledData;

    struct AirnodeData {
        bytes32 endpointId;
        address airnode;
        address sponsor;
        address sponsorWallet;
    }

    AirnodeData private airnodeData;

    // Make sure you specify the right _rrpAddress for your chain while deploying the contract.
    constructor(address _rrpAddress)
    RrpRequesterV0(_rrpAddress)
    Ownable(msg.sender)
    {}

    // To receive funds from the sponsor wallet and send them to the owner.
    receive() external payable {
        payable(owner()).transfer(address(this).balance);
    }

    function setAirnodeData(AirnodeData memory _airnodeData) external onlyOwner {
        airnodeData = _airnodeData;
    }

    // The main makeRequest function that will trigger the Airnode request.
    function makeRequest(address _walletAddress, uint256 _lastKnownNonce)
    external
    {
        bytes memory parameters = abi.encode(
            bytes32("1au"),
            bytes32("walletAddress"), _walletAddress,
            bytes32("lastKnownNonce"), _lastKnownNonce
        );

        bytes32 requestId = airnodeRrp.makeFullRequest(
            airnodeData.airnode, // airnode address
            airnodeData.endpointId, // endpointId
            airnodeData.sponsor, // sponsor's address
            airnodeData.sponsorWallet, // sponsorWallet
            address(this), // fulfillAddress
            this.fulfill.selector, // fulfillFunctionId
            parameters // encoded API parameters
        );
        incomingFulfillments[requestId] = true;
    }

    function fulfill(bytes32 _requestId, bytes calldata _data)
    external
    onlyAirnodeRrp
    {
        require(incomingFulfillments[_requestId], "No such request made");
        delete incomingFulfillments[_requestId];
        (
            address walletAddress,
            uint256 availablePoints,
            uint256 lastNonce
        ) = abi.decode(_data, (address, uint256, uint256));
        fulfilledData[walletAddress] = ApiResponse(availablePoints, lastNonce);
    }

    function getWalletPoints(address _userAddress)
    external
    view
    returns (ApiResponse memory)
    {
        return fulfilledData[_userAddress];
    }

    // To withdraw funds from the sponsor wallet to the contract.
    function withdraw(address _airnode, address _sponsorWallet)
    external
    onlyOwner
    {
        airnodeRrp.requestWithdrawal(_airnode, _sponsorWallet);
    }
}
