pragma solidity ^0.6.0;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/ERC777.sol";
import "@openzeppelin/contracts/token/ERC721/ERC20.sol";
import "@openzeppelin/contracts/token/ERC721/ERC1155.sol";

contract MyCollectible is ERC721 {
    constructor() ERC721("MyCollectible", "MCO") public {
    }
}