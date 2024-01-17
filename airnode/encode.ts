import {encode} from '@api3/airnode-abi';

// see https://docs.api3.org/reference/airnode/latest/packages/airnode-abi.html#encode
const parameters = [
  {type: 'address', name: 'walletAddress', value: '0x582eFe4e18Aae3c95faebfC9e39e35FE1E5a8Ae9'},
  {type: 'uint256', name: 'lastKnownNonce', value: 0},
];
const encodedData = encode(parameters);

console.log(encodedData);
