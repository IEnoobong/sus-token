import {HardhatUserConfig} from "hardhat/config";
import "hardhat-deploy";
import "@nomiclabs/hardhat-ethers";

import dotenv from 'dotenv';

dotenv.config();

const config: HardhatUserConfig = {
  solidity: "0.8.20",
  namedAccounts: {
    deployer: 0,
  },
  networks: {
    localhost: {
      url: 'http://127.0.0.1:8545',
      accounts: ['0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80',]
    },
    base_goerli: {
      url: 'https://goerli.base.org',
      accounts: [process.env.BASECAMP_PRIVATE_KEY ?? ''],
      verify: {
        etherscan: {
          apiUrl: "https://api-goerli.basescan.org",
          apiKey: process.env.ETHERSCAN_API_KEY ?? "ETHERSCAN_API_KEY"
        }
      }
    }
  }
};

export default config;
